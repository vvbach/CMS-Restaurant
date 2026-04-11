package vn.tts.service.layout;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.layout.MottoEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.layout.MottoPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.MottoHistoryResponse;
import vn.tts.model.response.layout.MottoResponse;
import vn.tts.repository.layout.MottoRepository;
import vn.tts.service.BaseService;
import vn.tts.service.MinioService;
import vn.tts.service.PublishableService;
import vn.tts.service.utils.PublishableHistoryUtils;
import vn.tts.service.utils.PublishingUtils;
import vn.tts.service.utils.QueryService;
import vn.tts.service.utils.ValidateEntityService;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class MottoService extends BaseService implements PublishableService<
        MottoResponse,
        MottoPayload,
        MottoPayload,
        MottoHistoryResponse
        >
{
    private final MottoRepository mottoRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_motto_publish";
    private final static String TOPIC_UNPUBLISH = "topic_motto_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<MottoEntity, MottoRepository> publishingUtils;
    private final QueryService<MottoEntity, MottoResponse, MottoRepository> queryService;
    private final ValidateEntityService<MottoEntity, MottoRepository> validateEntityService;
    private final PublishableHistoryUtils<MottoEntity, MottoHistoryResponse, MottoRepository> publishableHistoryUtils;


    public MottoService(
            KafkaTemplate<String, Object> kafkaTemplate,
            MottoRepository mottoRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.mottoRepository = mottoRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(mottoRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(mottoRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(mottoRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(mottoRepository);
    }

    @Override
    public List<MottoResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public MottoResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<MottoResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<MottoEntity> data = mottoRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<MottoResponse>> response = new PaginationResponse<>();
        response.setData(
                data.getContent()
                        .stream()
                        .map(this::getResponse)
                        .toList()
        );
        response.setTotal(data.getTotalElements());
        return response;
    }

    @Override
    @Transactional
    public MottoResponse create(MottoPayload payload) {
        MottoEntity entity = new MottoEntity(payload.getTitle(), payload.getDescription());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Motto has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(mottoRepository.save(entity));
    }

    @Override
    @Transactional
    public MottoResponse update(UUID id, MottoPayload payload) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUpdate(entity, "validate.article.status.is.draft.update");

        entity.setTitle(payload.getTitle());
        entity.setDescription(payload.getDescription());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Motto has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(mottoRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        mottoRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Motto has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<MottoHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, MottoHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Motto has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING_APPROVAL")
                        .message("Motto is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Motto has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Motto has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Motto has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        MottoEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Motto")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Motto has been moved back to draft.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private MottoResponse getResponse(MottoEntity entity) {
        return modelMapper.map(entity, MottoResponse.class);
    }
}
