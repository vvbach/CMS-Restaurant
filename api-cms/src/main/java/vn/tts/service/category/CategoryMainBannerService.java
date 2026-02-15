package vn.tts.service.category;

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
import vn.tts.entity.category.CategoryMainBannerEntity;
import vn.tts.entity.category.CategoryPageEntity;
import vn.tts.entity.food.FoodEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.enums.DeleteEnum;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryMainBannerPayload;
import vn.tts.model.payload.category.CategoryMainBannerUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryMainBannerHistoryResponse;
import vn.tts.model.response.category.CategoryMainBannerResponse;
import vn.tts.model.response.food.FoodCategoryResponse;
import vn.tts.repository.category.CategoryMainBannerRepository;
import vn.tts.repository.category.CategoryPageRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.repository.food.FoodRepository;
import vn.tts.service.base.CrudService;
import vn.tts.service.base.PublishableHistoryService;
import vn.tts.service.base.PublishingService;
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
public class CategoryMainBannerService extends BaseService implements
        CrudService<CategoryMainBannerResponse, CategoryMainBannerPayload, CategoryMainBannerUpdatePayload>,
        PublishingService,
        PublishableHistoryService<CategoryMainBannerHistoryResponse> {
    private final CategoryMainBannerRepository categoryMainBannerRepository;
    private final CategoryPageRepository categoryPageRepository;
    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final ModelMapper modelMapper;
    private final MinioService minioService;

    private final static String TOPIC_PUBLISH = "topic_category_main_banner_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_main_banner_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<CategoryMainBannerEntity, CategoryMainBannerRepository> publishingUtils;
    private final QueryService<CategoryMainBannerEntity, CategoryMainBannerResponse, CategoryMainBannerRepository> queryService;
    private final ValidateEntityService<CategoryMainBannerEntity, CategoryMainBannerRepository> validateEntityService;
    private final PublishableHistoryUtils<CategoryMainBannerEntity, CategoryMainBannerHistoryResponse, CategoryMainBannerRepository> publishableHistoryUtils;

    public CategoryMainBannerService(
            KafkaTemplate<String, Object> kafkaTemplate,
            CategoryMainBannerRepository categoryMainBannerRepository,
            CategoryPageRepository categoryPageRepository,
            FoodRepository foodRepository,
            FoodCategoryRepository foodCategoryRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.categoryMainBannerRepository = categoryMainBannerRepository;
        this.categoryPageRepository = categoryPageRepository;
        this.foodRepository = foodRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(categoryMainBannerRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(categoryMainBannerRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(categoryMainBannerRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(categoryMainBannerRepository);
    }

    public List<CategoryMainBannerResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    public List<CategoryMainBannerResponse> findAllByCategoryPageId(UUID categoryPageId) {
        return categoryMainBannerRepository.findAllByCategoryPageId(categoryPageId)
                .parallelStream().map(this::getResponse).toList();
    }

    public CategoryMainBannerResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    public PaginationResponse<List<CategoryMainBannerResponse>> filter(FilterPayload payload, UUID categoryPageId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<CategoryMainBannerEntity> data = categoryMainBannerRepository.filter(
                payload,
                categoryPageId,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable
        );

        PaginationResponse<List<CategoryMainBannerResponse>> response = new PaginationResponse<>();
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
    public CategoryMainBannerResponse create(CategoryMainBannerPayload payload) {
        validateFoodCategory(payload.getFoodId(), payload.getCategoryPageId());

        CategoryMainBannerEntity entity = new CategoryMainBannerEntity();
        entity.setCategoryPageId(payload.getCategoryPageId());
        entity.setFoodId(payload.getFoodId());
        entity.setTitle(payload.getTitle());
        entity.setDescription(payload.getDescription());
        entity.setImageUrl(payload.getImageUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Category Main Banner has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(categoryMainBannerRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryMainBannerResponse update(UUID id, CategoryMainBannerUpdatePayload payload) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateFoodCategory(payload.getFoodId(), entity.getCategoryPageId());
        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setFoodId(payload.getFoodId());
        entity.setTitle(payload.getTitle());
        entity.setDescription(payload.getDescription());
        entity.setImageUrl(payload.getImageUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Category Main Banner has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(categoryMainBannerRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        categoryMainBannerRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Category Main Banner has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<CategoryMainBannerHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, CategoryMainBannerHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Category Main Banner has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Category Main Banner is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Category Main Banner has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        validateFoodCategory(entity.getFoodId(), entity.getCategoryPageId());
        publishingUtils.checkPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Category Main Banner has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Category Main Banner has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        CategoryMainBannerEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Category Main Banner has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private CategoryMainBannerResponse getResponse(CategoryMainBannerEntity entity) {
        CategoryMainBannerResponse res = modelMapper.map(entity, CategoryMainBannerResponse.class);

        try {
            res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return res;
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
