package vn.tts.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.ImageWebEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.ImageWebPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.image.ImageWebHistoryResponse;
import vn.tts.model.response.image.ImageWebResponse;
import vn.tts.repository.ImageWebRepository;
import vn.tts.service.base.BaseQueryService;
import vn.tts.service.base.PublishableHistoryService;
import vn.tts.service.base.PublishingService;
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
public class ImageWebService extends BaseService implements
        BaseQueryService<ImageWebResponse>,
        PublishingService,
        PublishableHistoryService<ImageWebHistoryResponse>
{
    private final ImageWebRepository imageWebRepository;
    private final MinioService minioService;
    private final ModelMapper modelMapper;

    private final static String PATH_DEFAULT = "image/web/";

    private final static String TOPIC_PUBLISH = "topic_image_publish";
    private final static String TOPIC_UNPUBLISH = "topic_image_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<ImageWebEntity, ImageWebRepository> publishingUtils;
    private final QueryService<ImageWebEntity, ImageWebResponse, ImageWebRepository> queryService;
    private final ValidateEntityService<ImageWebEntity, ImageWebRepository> validateEntityService;
    private final PublishableHistoryUtils<ImageWebEntity, ImageWebHistoryResponse, ImageWebRepository> publishableHistoryUtils;

    public ImageWebService(
            KafkaTemplate<String, Object> kafkaTemplate,
            ImageWebRepository imageWebRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.imageWebRepository = imageWebRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(imageWebRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(imageWebRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(imageWebRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(imageWebRepository);
    }

    @Override
    public List<ImageWebResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public ImageWebResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<ImageWebResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<ImageWebEntity> data = imageWebRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<ImageWebResponse>> response = new PaginationResponse<>();
        response.setData(
                data.getContent()
                        .stream()
                        .map(this::getResponse)
                        .toList()
        );
        response.setTotal(data.getTotalElements());
        return response;
    }

    @Transactional
    public ImageWebResponse create(MultipartFile file, ImageWebPayload payload) throws Exception {
        ImageWebEntity entity = new ImageWebEntity(
                payload.getDescription(),
                uploadImageMinio(file, PATH_DEFAULT)
        );

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Image has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(imageWebRepository.save(entity));
    }

    @Transactional
    public ImageWebResponse update(UUID id, MultipartFile file, ImageWebPayload payload) throws Exception {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.image.status.is.revertToDraft.update");

        entity.setDescription(payload.getDescription());

        if (file != null) {
            validateImageFile(file);
            entity.setPathImage(uploadImageMinio(file, PATH_DEFAULT));
        }

        imageWebRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Image has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.image.status.is.revertToDraft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());

        imageWebRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Image has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<ImageWebHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, ImageWebHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.image.status.is.revertToDraft.reject");

        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Image has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.image.status.is.revertToDraft");

        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Image has been updated to pending approval stage.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.image.status.is.revertToDraft.approve");

        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Image has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPublish(entity, "validate.image.status.is.revertToDraft.publish");

        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Image has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.image.status.is.unpublish");

        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Image has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        ImageWebEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.image.status.is.revertToDraft.unpublish.revertToDraft");

        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Image")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Image has been updated to revertToDraft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private ImageWebResponse getResponse(ImageWebEntity entity) {
        String imageUrl;
        try {
            imageUrl = minioService.getPreSignedUrl(entity.getPathImage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return ImageWebResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .pathImage(imageUrl)
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
