package vn.tts.service;

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
import vn.tts.entity.AboutPageEntity;
import vn.tts.entity.BaseEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.AboutPagePayload;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.about.AboutPageHistoryResponse;
import vn.tts.model.response.about.AboutPageResponse;
import vn.tts.repository.AboutPageRepository;
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
public class AboutPageService extends BaseService implements PublishableService<
        AboutPageResponse,
        AboutPagePayload,
        AboutPagePayload,
        AboutPageHistoryResponse
        > {
    private final ModelMapper modelMapper;
    private final AboutPageRepository aboutPageRepository;

    private final static String TOPIC_PUBLISH = "topic_about_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_about_page_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<AboutPageEntity, AboutPageRepository> publishingUtils;
    private final QueryService<AboutPageEntity, AboutPageResponse, AboutPageRepository> queryService;
    private final ValidateEntityService<AboutPageEntity, AboutPageRepository> validateEntityService;
    private final PublishableHistoryUtils<AboutPageEntity, AboutPageHistoryResponse, AboutPageRepository> publishableHistoryUtils;

    public AboutPageService(
            KafkaTemplate<String, Object> kafkaTemplate,
            AboutPageRepository aboutPageRepository,
            ModelMapper modelMapper,
            MinioService minioService,
            BaseService baseService
    ) {
        this.aboutPageRepository = aboutPageRepository;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(aboutPageRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(aboutPageRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(aboutPageRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(aboutPageRepository);
    }

    @Override
    public List<AboutPageResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public AboutPageResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<AboutPageResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<AboutPageEntity> data = aboutPageRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<AboutPageResponse>> response = new PaginationResponse<>();
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
    public AboutPageResponse create(AboutPagePayload payload) {
        AboutPageEntity entity = modelMapper.map(payload, AboutPageEntity.class);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("PUBLISH")
                        .message("About Page has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(aboutPageRepository.save(entity));
    }

    @Override
    @Transactional
    public AboutPageResponse update(UUID id, AboutPagePayload payload) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setTitle(payload.getTitle());
        entity.setText(payload.getText());
        entity.setImageUrl(payload.getImageUrl());

        aboutPageRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("About Page content has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        aboutPageRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("About Page has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<AboutPageHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, AboutPageHistoryResponse.class))
                .stream()
                .map(Pair::getFirst)
                .peek(response -> {
                    String imageUrl = response.getImageUrl();

                    if (imageUrl != null && !imageUrl.isBlank()) {
                        try {
                            response.setImageUrl(minioService.getPreSignedUrl(imageUrl));
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
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.article.status.is.draft.reject");

        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("About Page has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.draft");

        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("About Page has been updated to pending approval stage.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.article.status.is.draft.approve");

        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("About Page has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPublish(entity, "validate.article.status.is.draft.publish");

        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("About Page has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");

        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);


        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("About Page has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        AboutPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.article.status.is.draft.unpublish.draft");

        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("About Page")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("About Page has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private AboutPageResponse getResponse(AboutPageEntity entity) {
        AboutPageResponse response = modelMapper.map(entity, AboutPageResponse.class);
        String imageUrl;
        try {
            imageUrl = minioService.getPreSignedUrl(response.getImageUrl());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        response.setImageUrl(imageUrl);
        return response;
    }
}
