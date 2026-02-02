package vn.tts.service.category;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.category.CategoryBestFoodEntity;
import vn.tts.entity.category.CategoryPageEntity;
import vn.tts.entity.food.FoodEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryBestFoodPayload;
import vn.tts.model.payload.category.CategoryBestFoodUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryBestFoodHistoryResponse;
import vn.tts.model.response.category.CategoryBestFoodResponse;
import vn.tts.model.response.food.FoodCategoryResponse;
import vn.tts.repository.category.CategoryBestFoodRepository;
import vn.tts.repository.category.CategoryPageRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.repository.food.FoodRepository;
import vn.tts.service.BaseService;
import vn.tts.service.MinioService;
import vn.tts.service.base.CrudService;
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
public class CategoryBestFoodService extends BaseService implements 
        CrudService<CategoryBestFoodResponse, CategoryBestFoodPayload, CategoryBestFoodUpdatePayload>,
        PublishingService,
        PublishableHistoryService<CategoryBestFoodHistoryResponse> {
    private final CategoryBestFoodRepository categoryBestFoodRepository;
    private final CategoryPageRepository categoryPageRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodRepository foodRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_category_best_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_best_food_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<CategoryBestFoodEntity, CategoryBestFoodRepository> publishingUtils;
    private final QueryService<CategoryBestFoodEntity, CategoryBestFoodResponse, CategoryBestFoodRepository> queryService;
    private final ValidateEntityService<CategoryBestFoodEntity, CategoryBestFoodRepository> validateEntityService;
    private final PublishableHistoryUtils<CategoryBestFoodEntity, CategoryBestFoodHistoryResponse, CategoryBestFoodRepository> publishableHistoryUtils;

    public CategoryBestFoodService(
            KafkaTemplate<String, Object> kafkaTemplate,
            CategoryBestFoodRepository categoryBestFoodRepository,
            CategoryPageRepository categoryPageRepository,
            FoodRepository foodRepository,
            FoodCategoryRepository foodCategoryRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.categoryBestFoodRepository = categoryBestFoodRepository;
        this.categoryPageRepository = categoryPageRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodRepository = foodRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(categoryBestFoodRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(categoryBestFoodRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(categoryBestFoodRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(categoryBestFoodRepository);
    }
    
    public List<CategoryBestFoodResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    public List<CategoryBestFoodResponse> findAllByCategoryPageId(UUID categoryPageId) {
        return categoryBestFoodRepository.findAllByCategoryPageId(categoryPageId)
                .parallelStream().map(this::getResponse).toList();
    }

    public CategoryBestFoodResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    public PaginationResponse<List<CategoryBestFoodResponse>> filter(FilterPayload payload, UUID categoryPageId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<CategoryBestFoodEntity> data = categoryBestFoodRepository.filter(
                payload,
                categoryPageId,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<CategoryBestFoodResponse>> response = new PaginationResponse<>();
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
    public CategoryBestFoodResponse create(CategoryBestFoodPayload payload) {
        validateFoodCategory(payload.getFoodId(), payload.getCategoryPageId());
        CategoryBestFoodEntity entity = new CategoryBestFoodEntity(
                payload.getCategoryPageId(),
                payload.getFoodId(),
                payload.getDescription()
        );

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Category Best Food has been created.")
                        .build(),
                TOPIC_NOTIFY
        );
        
        return getResponse(categoryBestFoodRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryBestFoodResponse update(UUID id, CategoryBestFoodUpdatePayload payload) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateFoodCategory(payload.getFoodId(), entity.getCategoryPageId());
        publishingUtils.checkUpdate(entity, "validate.article.status.is.revertToDraft.update");

        entity.setFoodId(payload.getFoodId());
        entity.setDescription(payload.getDescription());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Category Best Food has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(categoryBestFoodRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.revertToDraft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        categoryBestFoodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Category Best Food has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<CategoryBestFoodHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, CategoryBestFoodHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkReject(entity, "validate.article.status.is.revertToDraft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Category Best Food has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.revertToDraft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Category Best Food is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkApprove(entity, "validate.article.status.is.revertToDraft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Category Best Food has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        validateFoodCategory(entity.getFoodId(), entity.getCategoryPageId());
        publishingUtils.checkPublish(entity, "validate.article.status.is.revertToDraft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Category Best Food has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Category Best Food has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        CategoryBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkDraft(entity, "validate.article.status.is.revertToDraft.unpublish.revertToDraft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Category Best Food has been updated to revertToDraft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private CategoryBestFoodResponse getResponse(CategoryBestFoodEntity entity) {
        return modelMapper.map(entity, CategoryBestFoodResponse.class);
    }

    private void validateFoodCategory(UUID foodId, UUID categoryPageId) {
        FoodEntity entity = foodRepository.findById(foodId)
                .orElseThrow(() -> new AppBadRequestException("foodId", getMessage("validate.food.not.exist")));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("foodId", getMessage("validate.food.status.not.publish"));

        CategoryPageEntity categoryPageEntity = categoryPageRepository.findById(categoryPageId)
                .orElseThrow(() -> new AppBadRequestException("categoryPageId", getMessage("validate.category.page.not.exist")));

        List<UUID> categoryIds = foodCategoryRepository.findFoodCategoriesOfFoodId(foodId)
                .parallelStream().map(FoodCategoryResponse::getId).toList();

        if (!categoryIds.contains(categoryPageEntity.getCategoryId()))
            throw new AppBadRequestException("categoryId", getMessage("validate.food.category.not.compatible"));
    }
}
