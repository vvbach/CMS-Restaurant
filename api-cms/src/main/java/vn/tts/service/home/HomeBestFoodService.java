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
import vn.tts.entity.food.FoodEntity;
import vn.tts.entity.home.HomeBestFoodEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.enums.ContentStatus;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.home.HomeBestFoodPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.HomeBestFoodHistoryResponse;
import vn.tts.model.response.home.HomeBestFoodResponse;
import vn.tts.repository.food.FoodRepository;
import vn.tts.repository.home.HomeBestFoodRepository;
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
public class HomeBestFoodService extends BaseService implements PublishableService<
        HomeBestFoodResponse,
        HomeBestFoodPayload,
        HomeBestFoodPayload,
        HomeBestFoodHistoryResponse
        >
{
    private final HomeBestFoodRepository homeBestFoodRepository;
    private final FoodRepository foodRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_home_best_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_home_best_food_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<HomeBestFoodEntity, HomeBestFoodRepository> publishingUtils;
    private final QueryService<HomeBestFoodEntity, HomeBestFoodResponse, HomeBestFoodRepository> queryService;
    private final ValidateEntityService<HomeBestFoodEntity, HomeBestFoodRepository> validateEntityService;
    private final PublishableHistoryUtils<HomeBestFoodEntity, HomeBestFoodHistoryResponse, HomeBestFoodRepository> publishableHistoryUtils;

    public HomeBestFoodService(
            KafkaTemplate<String, Object> kafkaTemplate,
            HomeBestFoodRepository homeBestFoodRepository,
            FoodRepository foodRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.homeBestFoodRepository = homeBestFoodRepository;
        this.foodRepository = foodRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(homeBestFoodRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(homeBestFoodRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(homeBestFoodRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(homeBestFoodRepository);
    }

    @Override
    public List<HomeBestFoodResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public HomeBestFoodResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<HomeBestFoodResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<HomeBestFoodEntity> data = homeBestFoodRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<HomeBestFoodResponse>> response = new PaginationResponse<>();
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
    public HomeBestFoodResponse create(HomeBestFoodPayload payload) {
        validateFood(payload.getFoodId());

        HomeBestFoodEntity entity = new HomeBestFoodEntity(payload.getFoodId(), payload.getDescription());
        HomeBestFoodEntity savedEntity = homeBestFoodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(savedEntity.getId())
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Home Best Food has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(savedEntity);
    }

    @Override
    @Transactional
    public HomeBestFoodResponse update(UUID id, HomeBestFoodPayload payload) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateFood(payload.getFoodId());
        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setFoodId(payload.getFoodId());
        entity.setDescription(payload.getDescription());

        HomeBestFoodEntity updatedEntity = homeBestFoodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Home Best Food has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        homeBestFoodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Home Best Food has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<HomeBestFoodHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, HomeBestFoodHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Home Best Food has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Home Best Food is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Home Best Food has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        validateFood(entity.getFoodId());
        publishingUtils.checkPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Home Best Food has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Home Best Food has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        HomeBestFoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Best Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Home Best Food has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private HomeBestFoodResponse getResponse(HomeBestFoodEntity entity) {
        return modelMapper.map(entity, HomeBestFoodResponse.class);
    }

    private void validateFood(UUID foodId) {
        FoodEntity entity = foodRepository.findById(foodId)
                .orElseThrow(() -> new AppBadRequestException("foodId", getMessage("validate.food.not.exist")));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("foodId", getMessage("validate.food.status.not.publish"));
    }
}
