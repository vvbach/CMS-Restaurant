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
import vn.tts.entity.layout.SocialLinkEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.layout.SocialLinkPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.SocialLinkHistoryResponse;
import vn.tts.model.response.layout.SocialLinkResponse;
import vn.tts.repository.layout.SocialLinkRepository;
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
public class SocialLinkService extends BaseService implements PublishableService<
        SocialLinkResponse,
        SocialLinkPayload,
        SocialLinkPayload,
        SocialLinkHistoryResponse
        >
{
    private final SocialLinkRepository socialLinkRepository;
    private final MinioService minioService;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_social_link_publish";
    private final static String TOPIC_UNPUBLISH = "topic_social_link_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<SocialLinkEntity, SocialLinkRepository> publishingUtils;
    private final QueryService<SocialLinkEntity, SocialLinkResponse, SocialLinkRepository> queryService;
    private final ValidateEntityService<SocialLinkEntity, SocialLinkRepository> validateEntityService;
    private final PublishableHistoryUtils<SocialLinkEntity, SocialLinkHistoryResponse, SocialLinkRepository> publishableHistoryUtils;

    public SocialLinkService(
            KafkaTemplate<String, Object> kafkaTemplate,
            SocialLinkRepository socialLinkRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.socialLinkRepository = socialLinkRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(socialLinkRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(socialLinkRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(socialLinkRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(socialLinkRepository);
    }

    @Override
    public List<SocialLinkResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public SocialLinkResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<SocialLinkResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<SocialLinkEntity> data = socialLinkRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<SocialLinkResponse>> response = new PaginationResponse<>();
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
    public SocialLinkResponse create(SocialLinkPayload payload) {
        SocialLinkEntity entity = new SocialLinkEntity(
                payload.getUrl(),
                payload.getPlatform(),
                payload.getIconUrl()
        );

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Social Link has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(socialLinkRepository.save(entity));
    }

    @Override
    @Transactional
    public SocialLinkResponse update(UUID id, SocialLinkPayload payload) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setUrl(payload.getUrl());
        entity.setPlatform(payload.getPlatform());
        entity.setIconUrl(payload.getIconUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Social Link has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(socialLinkRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        socialLinkRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Social Link has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<SocialLinkHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, SocialLinkHistoryResponse.class))
                .stream()
                .map(Pair::getFirst)
                .peek(response -> {
                    String iconUrl = response.getIconUrl();

                    if (iconUrl != null && !iconUrl.isBlank()) {
                        try {
                            response.setIconUrl(minioService.getPreSignedUrl(iconUrl));
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
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Social Link has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING_APPROVAL")
                        .message("Social Link is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Social Link has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Social Link has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Social Link has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        SocialLinkEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Social Link")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Social Link has been moved back to draft.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private SocialLinkResponse getResponse(SocialLinkEntity entity) {
        String iconUrl;
        try {
            iconUrl = minioService.getPreSignedUrl(entity.getIconUrl());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return SocialLinkResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .platform(entity.getPlatform())
                .iconUrl(iconUrl)
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
