package vn.tts.service.food;

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
import vn.tts.entity.BaseEntity;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.food.FoodCategoryPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.food.FoodCategoryHistoryResponse;
import vn.tts.model.response.food.FoodCategoryResponse;
import vn.tts.repository.food.FoodCategoryRelationRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.service.PublishableService;
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
public class FoodCategoryService extends BaseService implements PublishableService<
        FoodCategoryResponse,
        FoodCategoryPayload,
        FoodCategoryPayload,
        FoodCategoryHistoryResponse
        >
{
    private final FoodCategoryRepository foodCategoryRepository;
    private final ModelMapper modelMapper;
    private final FoodCategoryRelationRepository foodCategoryRelationRepository;

    private final static String TOPIC_PUBLISH = "topic_food_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_food_category_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<FoodCategoryEntity, FoodCategoryRepository> publishingUtils;
    private final QueryService<FoodCategoryEntity, FoodCategoryResponse, FoodCategoryRepository> queryService;
    private final ValidateEntityService<FoodCategoryEntity, FoodCategoryRepository> validateEntityService;
    private final PublishableHistoryUtils<FoodCategoryEntity, FoodCategoryHistoryResponse, FoodCategoryRepository> publishableHistoryUtils;

    public FoodCategoryService(
            FoodCategoryRepository foodCategoryRepository,
            FoodCategoryRelationRepository foodCategoryRelationRepository,
            ModelMapper modelMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            BaseService baseService
    ) {
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodCategoryRelationRepository = foodCategoryRelationRepository;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(foodCategoryRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(foodCategoryRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(foodCategoryRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(foodCategoryRepository);
    }

    @Override
    public List<FoodCategoryResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public FoodCategoryResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<FoodCategoryResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<FoodCategoryEntity> data = foodCategoryRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<FoodCategoryResponse>> response = new PaginationResponse<>();
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
    public FoodCategoryResponse create(FoodCategoryPayload payload) {
        if (foodCategoryRepository.existsByName(payload.getName()))
            throw new AppBadRequestException("name", getMessage("validate.food.category.name.already.exists"));

        FoodCategoryEntity entity = new FoodCategoryEntity(
                payload.getName(),
                payload.getDescription()
        );

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Food Category has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(foodCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public FoodCategoryResponse update(UUID id, FoodCategoryPayload payload) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUpdate(entity, "validate.food.category.status.is.draft.update");

        entity.setName(payload.getName());
        entity.setDescription(payload.getDescription());
        foodCategoryRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Food Category has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDelete(entity, "validate.food.category.status.is.draft.delete");

        foodCategoryRelationRepository.deleteAllByFoodCategoryId(entity.getId());

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        foodCategoryRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Food Category has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<FoodCategoryHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, FoodCategoryHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForReject(entity, "validate.food.category.status.is.draft.reject");

        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Food Category has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPendingApproval(entity, "validate.food.category.status.is.draft");

        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Food Category is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForApprove(entity, "validate.food.category.status.is.draft.approve");

        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Food Category has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForPublish(entity, "validate.food.category.status.is.draft.publish");

        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Food Category has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForUnpublish(entity, "validate.food.category.status.is.unpublish");

        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Food Category has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        FoodCategoryEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDraft(entity, "validate.food.category.status.is.draft.unpublish.draft");

        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food Category")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Food Category has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private FoodCategoryResponse getResponse(FoodCategoryEntity entity) {
        return modelMapper.map(
                entity,
                FoodCategoryResponse.class
        );
    }
}
