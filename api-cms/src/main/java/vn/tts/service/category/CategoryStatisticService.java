package vn.tts.service.category;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.category.CategoryStatisticEntity;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryStatisticPayload;
import vn.tts.model.payload.category.CategoryStatisticUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryStatisticHistoryResponse;
import vn.tts.model.response.category.CategoryStatisticResponse;
import vn.tts.repository.category.CategoryPageRepository;
import vn.tts.repository.category.CategoryStatisticRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.service.base.CrudService;
import vn.tts.service.base.PublishableHistoryService;
import vn.tts.service.base.PublishingService;
import vn.tts.service.BaseService;
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
public class CategoryStatisticService extends BaseService implements
        CrudService<CategoryStatisticResponse, CategoryStatisticPayload, CategoryStatisticUpdatePayload>,
        PublishingService,
        PublishableHistoryService<CategoryStatisticHistoryResponse> {
    private final CategoryStatisticRepository categoryStatisticRepository;
    private final CategoryPageRepository categoryPageRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_category_statistic_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_statistic_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<CategoryStatisticEntity, CategoryStatisticRepository> publishingUtils;
    private final QueryService<CategoryStatisticEntity, CategoryStatisticResponse, CategoryStatisticRepository> queryService;
    private final ValidateEntityService<CategoryStatisticEntity, CategoryStatisticRepository> validateEntityService;
    private final PublishableHistoryUtils<CategoryStatisticEntity, CategoryStatisticHistoryResponse, CategoryStatisticRepository> publishableHistoryUtils;

    @Autowired
    public CategoryStatisticService(
            CategoryStatisticRepository categoryStatisticRepository,
            CategoryPageRepository categoryPageRepository,
            FoodCategoryRepository foodCategoryRepository,
            ModelMapper modelMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            BaseService baseService
    ) {
        this.categoryStatisticRepository = categoryStatisticRepository;
        this.categoryPageRepository = categoryPageRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(categoryStatisticRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(categoryStatisticRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(categoryStatisticRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(categoryStatisticRepository);
    }

    public List<CategoryStatisticResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    public List<CategoryStatisticResponse> findAll(UUID categoryPageId) {
        return categoryStatisticRepository.findAllByCategoryPageId(categoryPageId)
                .parallelStream().map(this::getResponse).toList();
    }

    public CategoryStatisticResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    public PaginationResponse<List<CategoryStatisticResponse>> filter(FilterPayload payload, UUID categoryPageId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<CategoryStatisticEntity> data = categoryStatisticRepository.filter(
                payload,
                categoryPageId,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<CategoryStatisticResponse>> response = new PaginationResponse<>();
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
    public CategoryStatisticResponse create(CategoryStatisticPayload payload) {
        validateCategoryAndPage(payload.getCategoryPageId(), payload.getCategoryId());
        CategoryStatisticEntity entity = new CategoryStatisticEntity(
                payload.getCategoryPageId(),
                payload.getCategoryId(),
                payload.getName(),
                payload.getDescription(),
                payload.getImageUrl()
        );

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Category Statistic has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(categoryStatisticRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryStatisticResponse update(UUID id, CategoryStatisticUpdatePayload payload) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateCategoryAndPage(entity.getCategoryPageId(), payload.getCategoryId());
        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setCategoryId(payload.getCategoryId());
        entity.setName(payload.getName());
        entity.setDescription(payload.getDescription());
        entity.setImageUrl(payload.getImageUrl());

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Category Statistic has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(categoryStatisticRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        categoryStatisticRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Category Statistic has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<CategoryStatisticHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, CategoryStatisticHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkReject(entity, "validate.article.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Category Statistic has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPendingApproval(entity, "validate.article.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Category Statistic is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkApprove(entity, "validate.article.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Category Statistic has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        validateCategoryAndPage(entity.getCategoryPageId(), entity.getCategoryId());
        publishingUtils.checkPublish(entity, "validate.article.is.draft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Category Statistic has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkUnpublish(entity, "validate.article.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Category Statistic has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        CategoryStatisticEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkDraft(entity, "validate.article.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Statistic")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Category Statistic has been updated to revertToDraft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private CategoryStatisticResponse getResponse(CategoryStatisticEntity entity) {
        CategoryStatisticResponse res = modelMapper.map(entity, CategoryStatisticResponse.class);

        try {
            res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return res;
    }

    private void validateCategoryAndPage(UUID categoryPageId, UUID categoryId) {
        FoodCategoryEntity entity = foodCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppBadRequestException("categoryId", getMessage("validate.food.category.not.exist")));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("categoryId", getMessage("validate.food.category.not.publish"));

        categoryPageRepository.findById(categoryPageId)
                .orElseThrow(() -> new AppBadRequestException("categoryPageId", getMessage("validate.category.page.not.exist")));
    }
}
