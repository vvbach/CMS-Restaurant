package vn.tts.service.home;

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
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.entity.home.FeaturedCategoryEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.enums.ContentStatus;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.home.FeaturedCategoryPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.FeaturedCategoryHistoryResponse;
import vn.tts.model.response.home.FeaturedCategoryResponse;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.repository.home.FeaturedCategoryRepository;
import vn.tts.service.PublishableService;
import vn.tts.service.BaseService;
import vn.tts.service.MinioService;
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
public class FeaturedCategoryService extends BaseService implements PublishableService<
        FeaturedCategoryResponse,
        FeaturedCategoryPayload,
        FeaturedCategoryPayload,
        FeaturedCategoryHistoryResponse
        >
{
    private final FeaturedCategoryRepository featuredCategoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_featured_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_featured_category_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<FeaturedCategoryEntity, FeaturedCategoryRepository> publishingUtils;
    private final QueryService<FeaturedCategoryEntity, FeaturedCategoryResponse, FeaturedCategoryRepository> queryService;
    private final ValidateEntityService<FeaturedCategoryEntity, FeaturedCategoryRepository> validateEntityService;
    private final PublishableHistoryUtils<FeaturedCategoryEntity, FeaturedCategoryHistoryResponse, FeaturedCategoryRepository> publishableHistoryUtils;


    public FeaturedCategoryService(
            KafkaTemplate<String, Object> kafkaTemplate,
            FeaturedCategoryRepository featuredCategoryRepository,
            FoodCategoryRepository foodCategoryRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.featuredCategoryRepository = featuredCategoryRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(featuredCategoryRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(featuredCategoryRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(featuredCategoryRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(featuredCategoryRepository);
    }

    @Override
    public List<FeaturedCategoryResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public FeaturedCategoryResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<FeaturedCategoryResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<FeaturedCategoryEntity> data = featuredCategoryRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<FeaturedCategoryResponse>> response = new PaginationResponse<>();
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
    public FeaturedCategoryResponse create(FeaturedCategoryPayload payload) {
        validateCategory(payload.getCategoryId());
        FeaturedCategoryEntity entity = new FeaturedCategoryEntity(
                payload.getCategoryId(),
                payload.getImageUrl(),
                payload.getDescription()
        );
        FeaturedCategoryEntity saved = featuredCategoryRepository.save(entity);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(saved.getId())
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail()))
                .action("CREATE")
                .message("Featured Category has been created.")
                .build(),
            TOPIC_NOTIFY
        );
        return getResponse(saved);
    }

    @Override
    @Transactional
    public FeaturedCategoryResponse update(UUID id, FeaturedCategoryPayload payload) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        validateCategory(payload.getCategoryId());
        publishingUtils.checkForUpdate(entity, "validate.article.status.is.draft.update");
        entity.setCategoryId(payload.getCategoryId());
        entity.setImageUrl(payload.getImageUrl());
        entity.setDescription(payload.getDescription());
        FeaturedCategoryEntity saved = featuredCategoryRepository.save(entity);

        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("UPDATE")
                .message("Featured Category has been updated.")
                .build(),
            TOPIC_NOTIFY
        );
        return getResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForDelete(entity, "validate.article.status.is.draft.delete");
        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        featuredCategoryRepository.save(entity);

        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("DELETE")
                .message("Featured Category has been deleted.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    public List<FeaturedCategoryHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, FeaturedCategoryHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("REJECT")
                .message("Featured Category has been rejected.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("PENDING APPROVAL")
                .message("Featured Category is pending approval.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("APPROVE")
                .message("Featured Category has been approved.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        validateCategory(entity.getCategoryId());
        publishingUtils.checkForPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("PUBLISH")
                .message("Featured Category has been published.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("UNPUBLISH")
                .message("Featured Category has been unpublished.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        FeaturedCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            vn.tts.model.event.SendEmailEvent.builder()
                .entityId(id)
                .entityType("FeaturedCategory")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("DRAFT")
                .message("Featured Category has been updated to draft state.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    private FeaturedCategoryResponse getResponse(FeaturedCategoryEntity entity) {
        FeaturedCategoryResponse response = modelMapper.map(entity, FeaturedCategoryResponse.class);

        try {
            response.setImageUrl(minioService.getPreSignedUrl(entity.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
    }

    private void validateCategory(UUID categoryId) {
        FoodCategoryEntity entity = foodCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppBadRequestException("categoryId", getMessage("validate.food.category.not.exist")));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("categoryId", getMessage("validate.food.category.not.publish"));
    }
}
