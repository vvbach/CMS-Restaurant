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
import vn.tts.entity.category.CategoryPageEntity;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryPagePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryPageHistoryResponse;
import vn.tts.model.response.category.CategoryPageResponse;
import vn.tts.repository.category.CategoryPageRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.service.BaseService;
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
@Slf4j
public class CategoryPageService extends BaseService implements
        CrudService<CategoryPageResponse, CategoryPagePayload, CategoryPagePayload>,
        PublishingService,
        PublishableHistoryService<CategoryPageHistoryResponse>
{
    private final CategoryPageRepository categoryPageRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_category_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_page_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<CategoryPageEntity, CategoryPageRepository> publishingUtils;
    private final QueryService<CategoryPageEntity, CategoryPageResponse, CategoryPageRepository> queryService;
    private final ValidateEntityService<CategoryPageEntity, CategoryPageRepository> validateEntityService;
    private final PublishableHistoryUtils<CategoryPageEntity, CategoryPageHistoryResponse, CategoryPageRepository> publishableHistoryUtils;

    @Autowired
    public CategoryPageService(
            CategoryPageRepository categoryPageRepository,
            FoodCategoryRepository foodCategoryRepository,
            ModelMapper modelMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            BaseService baseService
    ) {
        this.categoryPageRepository = categoryPageRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(categoryPageRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(categoryPageRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(categoryPageRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(categoryPageRepository);
    }

    public List<CategoryPageResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    public CategoryPageResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    public PaginationResponse<List<CategoryPageResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<CategoryPageEntity> data = categoryPageRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<CategoryPageResponse>> response = new PaginationResponse<>();
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
    public CategoryPageResponse create(CategoryPagePayload payload) {
        validateCategory(payload.getCategoryId());
        CategoryPageEntity entity = new CategoryPageEntity(
                payload.getCategoryId(),
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

        return getResponse(categoryPageRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryPageResponse update(UUID id, CategoryPagePayload payload) {
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateCategory(payload.getCategoryId());
        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setCategoryId(payload.getCategoryId());
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

        return getResponse(categoryPageRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        categoryPageRepository.save(entity);

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
    public List<CategoryPageHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, CategoryPageHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkReject(entity, "validate.article.is.draft.reject");
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
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPendingApproval(entity, "validate.article.is.draft");
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
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkApprove(entity, "validate.article.is.draft.approve");
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
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        validateCategory(entity.getCategoryId());
        publishingUtils.checkPublish(entity, "validate.article.is.draft.publish");
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
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkUnpublish(entity, "validate.article.is.unpublish");
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
        CategoryPageEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkDraft(entity, "validate.article.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Category Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Category Best Food has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private CategoryPageResponse getResponse(CategoryPageEntity entity) {
        return modelMapper.map(entity, CategoryPageResponse.class);
    }

    private void validateCategory(UUID categoryId) {
        FoodCategoryEntity entity = foodCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppBadRequestException("categoryId", getMessage("validate.food.category.not.exist")));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("categoryId", getMessage("validate.food.category.not.publish"));
    }
}
