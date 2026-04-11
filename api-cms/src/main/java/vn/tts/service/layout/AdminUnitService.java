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
import vn.tts.entity.layout.AdminUnitEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.layout.AdminUnitPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.AdminUnitHistoryResponse;
import vn.tts.model.response.layout.AdminUnitResponse;
import vn.tts.repository.layout.AdminUnitRepository;
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
public class AdminUnitService extends BaseService implements PublishableService<
        AdminUnitResponse,
        AdminUnitPayload,
        AdminUnitPayload,
        AdminUnitHistoryResponse
        >
{
    private final AdminUnitRepository adminUnitRepository;
    private final MinioService minioService;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_admin_unit_publish";
    private final static String TOPIC_UNPUBLISH = "topic_admin_unit_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<AdminUnitEntity, AdminUnitRepository> publishingUtils;
    private final QueryService<AdminUnitEntity, AdminUnitResponse, AdminUnitRepository> queryService;
    private final ValidateEntityService<AdminUnitEntity, AdminUnitRepository> validateEntityService;
    private final PublishableHistoryUtils<AdminUnitEntity, AdminUnitHistoryResponse, AdminUnitRepository> publishableHistoryUtils;


    public AdminUnitService(
            KafkaTemplate<String, Object> kafkaTemplate,
            AdminUnitRepository adminUnitRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.adminUnitRepository = adminUnitRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(adminUnitRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(adminUnitRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(adminUnitRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(adminUnitRepository);
    }

    @Override
    public List<AdminUnitResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public AdminUnitResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<AdminUnitResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<AdminUnitEntity> data = adminUnitRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<AdminUnitResponse>> response = new PaginationResponse<>();
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
    public AdminUnitResponse create(AdminUnitPayload payload) {
        AdminUnitEntity entity = new AdminUnitEntity(payload.getName(), payload.getLogoUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Admin Unit has been created.")
                        .build(),
                TOPIC_NOTIFY
        );


        return getResponse(adminUnitRepository.save(entity));
    }

    @Override
    @Transactional
    public AdminUnitResponse update(UUID id, AdminUnitPayload payload) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUpdate(entity, "validate.article.status.is.draft.update");


        entity.setName(payload.getName());
        entity.setLogoUrl(payload.getLogoUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Admin Unit has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(adminUnitRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        adminUnitRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Admin Unit has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<AdminUnitHistoryResponse> history(UUID id) {
        return publishableHistoryUtils
                .getUpdatedHistoryRevisions(id,
                        entity -> modelMapper.map(entity, AdminUnitHistoryResponse.class))
                .stream()
                .map(Pair::getFirst)
                .peek(response -> {
                    String logoUrl = response.getLogoUrl();

                    if (logoUrl != null && !logoUrl.isBlank()) {
                        try {
                            response.setLogoUrl(minioService.getPreSignedUrl(logoUrl));
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
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Admin Unit has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING_APPROVAL")
                        .message("Admin Unit is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Admin Unit has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Admin Unit has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Admin Unit has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        AdminUnitEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Admin Unit")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Admin Unit has been moved back to draft.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private AdminUnitResponse getResponse(AdminUnitEntity entity) {
        String logoUrl = entity.getLogoUrl();
        try {
            logoUrl = minioService.getPreSignedUrl(logoUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return AdminUnitResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .logoUrl(logoUrl)
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
