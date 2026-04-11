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
import vn.tts.entity.layout.LogoPageEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.layout.LogoPagePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.LogoPageHistoryResponse;
import vn.tts.model.response.layout.LogoPageResponse;
import vn.tts.repository.layout.LogoPageRepository;
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
public class LogoPageService extends BaseService implements PublishableService<
        LogoPageResponse,
        LogoPagePayload,
        LogoPagePayload,
        LogoPageHistoryResponse
        > 
{
    private final LogoPageRepository logoPageRepository;
    private final MinioService minioService;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_logo_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_logo_page_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<LogoPageEntity, LogoPageRepository> publishingUtils;
    private final QueryService<LogoPageEntity, LogoPageResponse, LogoPageRepository> queryService;
    private final ValidateEntityService<LogoPageEntity, LogoPageRepository> validateEntityService;
    private final PublishableHistoryUtils<LogoPageEntity, LogoPageHistoryResponse, LogoPageRepository> publishableHistoryUtils;

    public LogoPageService(
            KafkaTemplate<String, Object> kafkaTemplate,
            LogoPageRepository logoPageRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.logoPageRepository = logoPageRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(logoPageRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(logoPageRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(logoPageRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(logoPageRepository);
    }

    @Override
    public List<LogoPageResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public LogoPageResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<LogoPageResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<LogoPageEntity> data = logoPageRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<LogoPageResponse>> response = new PaginationResponse<>();
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
    public LogoPageResponse create(LogoPagePayload payload) {
        LogoPageEntity entity = new LogoPageEntity(payload.getName(), payload.getUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Logo Page has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(logoPageRepository.save(entity));
    }

    @Override
    @Transactional
    public LogoPageResponse update(UUID id, LogoPagePayload payload) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUpdate(entity, "validate.article.status.is.draft.update");

        entity.setName(payload.getName());
        entity.setUrl(payload.getUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Logo Page has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(logoPageRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        logoPageRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Logo Page has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<LogoPageHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, LogoPageHistoryResponse.class))
                .stream()
                .map(Pair::getFirst)
                .peek(response -> {
                    String logoUrl = response.getUrl();

                    if (logoUrl != null && !logoUrl.isBlank()) {
                        try {
                            response.setUrl(minioService.getPreSignedUrl(logoUrl));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }

                })
                .toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Logo Page has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING_APPROVAL")
                        .message("Logo Page is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Logo Page has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Logo Page has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Logo Page has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        LogoPageEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Logo Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Logo Page has been moved back to draft.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private LogoPageResponse getResponse(LogoPageEntity entity) {
        String url;
        try {
            url = minioService.getPreSignedUrl(entity.getUrl());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return LogoPageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(url)
                .isDelete(entity.getIsDelete())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .deletionReason(entity.getDeletionReason())
                .unpublishReason(entity.getUnpublishReason())
                .createdByName(entity.getCreatedByName())
                .createdAt(entity.getCreatedAt())
                .updatedByName(entity.getUpdatedByName())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
