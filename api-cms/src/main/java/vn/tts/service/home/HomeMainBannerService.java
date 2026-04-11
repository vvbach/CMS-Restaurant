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
import vn.tts.entity.home.HomeMainBannerEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.enums.ContentStatus;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.home.HomeMainBannerPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.HomeMainBannerHistoryResponse;
import vn.tts.model.response.home.HomeMainBannerResponse;
import vn.tts.repository.food.FoodRepository;
import vn.tts.repository.home.HomeMainBannerRepository;
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
public class HomeMainBannerService extends BaseService implements PublishableService<
        HomeMainBannerResponse,
        HomeMainBannerPayload,
        HomeMainBannerPayload,
        HomeMainBannerHistoryResponse
        >
{
    private final HomeMainBannerRepository homeMainBannerRepository;
    private final FoodRepository foodRepository;
    private final ModelMapper modelMapper;
    private final MinioService minioService;

    private final static String TOPIC_PUBLISH = "topic_home_main_banner_publish";
    private final static String TOPIC_UNPUBLISH = "topic_home_main_banner_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<HomeMainBannerEntity, HomeMainBannerRepository> publishingUtils;
    private final QueryService<HomeMainBannerEntity, HomeMainBannerResponse, HomeMainBannerRepository> queryService;
    private final ValidateEntityService<HomeMainBannerEntity, HomeMainBannerRepository> validateEntityService;
    private final PublishableHistoryUtils<HomeMainBannerEntity, HomeMainBannerHistoryResponse, HomeMainBannerRepository> publishableHistoryUtils;

    public HomeMainBannerService(
            KafkaTemplate<String, Object> kafkaTemplate,
            HomeMainBannerRepository homeMainBannerRepository,
            FoodRepository foodRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.homeMainBannerRepository = homeMainBannerRepository;
        this.foodRepository = foodRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(homeMainBannerRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(homeMainBannerRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(homeMainBannerRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(homeMainBannerRepository);
    }

    @Override
    public List<HomeMainBannerResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public HomeMainBannerResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<HomeMainBannerResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<HomeMainBannerEntity> data = homeMainBannerRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<HomeMainBannerResponse>> response = new PaginationResponse<>();
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
    public HomeMainBannerResponse create(HomeMainBannerPayload payload) {
        validateFood(payload.getFoodId());
        HomeMainBannerEntity entity = new HomeMainBannerEntity();
        entity.setFoodId(payload.getFoodId());
        entity.setTitle(payload.getTitle());
        entity.setDescription(payload.getDescription());
        entity.setImageUrl(payload.getImageUrl());

        homeMainBannerRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Home Main Banner has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public HomeMainBannerResponse update(UUID id, HomeMainBannerPayload payload) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        validateFood(payload.getFoodId());
        publishingUtils.checkForUpdate(entity, "validate.article.status.is.draft.update");

        entity.setFoodId(payload.getFoodId());
        entity.setTitle(payload.getTitle());
        entity.setDescription(payload.getDescription());
        entity.setImageUrl(payload.getImageUrl());

        homeMainBannerRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Home Main Banner has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");

        publishingUtils.checkForDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        homeMainBannerRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Home Main Banner has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<HomeMainBannerHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, HomeMainBannerHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForReject(entity, "validate.article.status.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Home Main Banner has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForPendingApproval(entity, "validate.article.status.is.draft");
        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Home Main Banner is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForApprove(entity, "validate.article.status.is.draft.approve");
        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Home Main Banner has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        validateFood(entity.getFoodId());
        publishingUtils.checkForPublish(entity, "validate.article.status.is.draft.publish");
        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Home Main Banner has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForUnpublish(entity, "validate.article.status.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Home Main Banner has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        HomeMainBannerEntity entity = validateEntityService.getValidEntity(id, "message.entity.not.found");
        publishingUtils.checkForDraft(entity, "validate.article.status.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Home Main Banner")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Home Main Banner has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }


    private HomeMainBannerResponse getResponse(HomeMainBannerEntity entity) {
        HomeMainBannerResponse res = modelMapper.map(entity, HomeMainBannerResponse.class);

        try {
            res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return res;
    }

    private void validateFood(UUID foodId) {
        FoodEntity entity = foodRepository.findById(foodId)
                .orElseThrow(() -> new AppBadRequestException("foodId", "validate.food.not.exist"));

        if (!entity.getStatus().equals(ContentStatus.PUBLISHED))
            throw new AppBadRequestException("foodId", "validate.food.status.not.publish");
    }
}
