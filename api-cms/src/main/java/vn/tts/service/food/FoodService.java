package vn.tts.service.food;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.Revision;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.entity.food.FoodCategoryRelation;
import vn.tts.entity.food.FoodEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.exception.AppBadRequestException;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.dto.FoodCategoryDto;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.food.FoodPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.food.*;
import vn.tts.repository.food.FoodCategoryRelationRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.repository.food.FoodRepository;
import vn.tts.service.PublishableService;
import vn.tts.service.food.FoodService;
import vn.tts.service.BaseService;
import vn.tts.service.MinioService;
import vn.tts.service.utils.PublishableHistoryUtils;
import vn.tts.service.utils.PublishingUtils;
import vn.tts.service.utils.QueryService;
import vn.tts.service.utils.ValidateEntityService;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class FoodService extends BaseService implements PublishableService<
        FoodResponse,
        FoodPayload,
        FoodPayload,
        FoodHistoryResponse
        >
{
    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodCategoryRelationRepository foodCategoryRelationRepository;
    private final ModelMapper modelMapper;
    private final MinioService minioService;

    private final static String TOPIC_PUBLISH = "topic_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_food_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<FoodEntity, FoodRepository> publishingUtils;
    private final QueryService<FoodEntity, FoodResponse, FoodRepository> queryService;
    private final ValidateEntityService<FoodEntity, FoodRepository> validateEntityService;
    private final PublishableHistoryUtils<FoodEntity, FoodHistoryResponse, FoodRepository> publishableHistoryUtils;

    @Autowired
    public FoodService(
            FoodRepository foodRepository,
            FoodCategoryRepository foodCategoryRepository,
            FoodCategoryRelationRepository foodCategoryRelationRepository,
            ModelMapper modelMapper,
            MinioService minioService,
            KafkaTemplate<String, Object> kafkaTemplate,
            BaseService baseService
    ) {
        this.foodRepository = foodRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodCategoryRelationRepository = foodCategoryRelationRepository;
        this.modelMapper = modelMapper;
        this.minioService = minioService;
        this.publishingUtils = new PublishingUtils<>(foodRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(foodRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(foodRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(foodRepository);
    }

    public List<FoodResponse> findByCategoryName(String categoryName) {
        List<FoodEntity> foodEntities = foodRepository.findAllByCategoryName(categoryName);
        return foodEntities.stream()
                .map(this::getResponse)
                .toList();
    }

    public List<FoodResponse> findByCategoryId(UUID categoryId) {
        return foodRepository.findAllByCategoryId(categoryId).stream()
                .map(this::getResponse)
                .toList();
    }


    @Override
    public List<FoodResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public FoodResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<FoodResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<FoodEntity> data = foodRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<FoodResponse>> response = new PaginationResponse<>();
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
    public FoodResponse create(FoodPayload payload) {
        FoodEntity foodEntity = modelMapper.map(payload, FoodEntity.class);

        FoodEntity createdEntity = foodRepository.save(foodEntity);
        UUID foodId = createdEntity.getId();

        if (payload.getCategoryIds().isEmpty()) {
            return getResponse(createdEntity);
        }

        Set<UUID> categoryIds = new HashSet<>(payload.getCategoryIds());

        List<FoodCategoryEntity> categories = foodCategoryRepository.findAllById(categoryIds);
        validateCategories(categories, categoryIds);

        List<FoodCategoryRelation> relations = categoryIds.stream().map(categoryId -> {
            FoodCategoryRelation foodCategoryRelation = new FoodCategoryRelation();
            foodCategoryRelation.setFoodId(foodId);
            foodCategoryRelation.setFoodCategoryId(categoryId);
            return foodCategoryRelation;
        }).toList();

        foodCategoryRelationRepository.saveAll(relations);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(foodId)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Food has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(foodEntity);
    }

    @Override
    @Transactional
    public FoodResponse update(UUID id, FoodPayload payload) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.food.status.is.draft.update");

        entity.setName(payload.getName());
        entity.setDescription(payload.getDescription());
        entity.setPrice(payload.getPrice());
        entity.setImageUrl(payload.getImageUrl());
        entity.setDiscount(payload.getDiscount());
        entity.setStockQuantity(payload.getStockQuantity());
        entity.setUpdatedAt(Instant.now());

        Set<UUID> categoryIds = new HashSet<>(payload.getCategoryIds());
        List<FoodCategoryEntity> categories = foodCategoryRepository.findAllById(categoryIds);
        validateCategories(categories, categoryIds);

        foodCategoryRelationRepository.deleteByFoodId(id);

        List<FoodCategoryRelation> relations = categoryIds.stream().map(categoryId -> {
            FoodCategoryRelation relation = new FoodCategoryRelation();
            relation.setFoodId(id);
            relation.setFoodCategoryId(categoryId);
            return relation;
        }).toList();

        foodCategoryRelationRepository.saveAll(relations);

        foodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Food has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.food.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        foodRepository.save(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Food has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<FoodHistoryResponse> history(UUID id) {
        List<Pair<FoodHistoryResponse, Revision<Integer, FoodEntity>>> pairs = publishableHistoryUtils
                .getUpdatedHistoryRevisions(id, entity -> modelMapper.map(entity, FoodHistoryResponse.class));

        Map<Integer, List<FoodCategoryResponse>> categoryMap = new HashMap<>();

        List<FoodCategoryDto> dtos = foodCategoryRepository.findFoodCategoriesByRevNumbers(
                pairs.stream().map(pair -> pair.getSecond().getRevisionNumber().orElseThrow(
                        () -> new RuntimeException("Rev number is null")
                )).toList()
        );

        dtos.forEach(dto -> {
            FoodCategoryResponse response = new FoodCategoryResponse(
                    dto.getId(),
                    dto.getName(),
                    dto.getDescription()
            );

            response.setStatus(ContentStatus.valueOf(dto.getStatus()));
            response.setIsDelete(dto.getIsDelete() == 0 ? DeleteEnum.NO : DeleteEnum.YES);
            response.setCreatedAt(dto.getCreatedAt());
            response.setCreatedByName(dto.getCreatedByName());
            response.setUpdatedAt(dto.getUpdatedAt());
            response.setUpdatedByName(dto.getUpdatedByName());
            response.setDeletionReason(dto.getDeletionReason());
            response.setRejectionReason(dto.getRejectionReason());
            response.setUnpublishReason(dto.getUnpublishReason());

            if (!categoryMap.containsKey(dto.getRev()))
                categoryMap.put(dto.getRev(), new ArrayList<>());

            categoryMap.get(dto.getRev()).add(response);
        });

        // create a sorted map so we can look up floorEntry (largest key <= rev)
        NavigableMap<Integer, List<FoodCategoryResponse>> sortedMap =
                new TreeMap<>(categoryMap);

        // produce responses: if exact rev not found, use floorEntry(rev), otherwise empty list
        return pairs.stream().map(pair -> {
            FoodHistoryResponse response = pair.getFirst();
            int rev = pair.getSecond().getRevisionNumber()
                    .orElseThrow(() -> new RuntimeException("Rev number is null"));

            List<FoodCategoryResponse> cats = sortedMap.get(rev);
            if (cats == null) {
                Map.Entry<Integer, List<FoodCategoryResponse>> floor = sortedMap.floorEntry(rev);
                cats = (floor != null) ? floor.getValue() : Collections.emptyList();
            }

            // defensive copy to avoid accidental external mutation
            response.setCategories(new ArrayList<>(cats));
            return response;
        }).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.food.status.is.draft.reject");

        publishingUtils.rejectEntity(entity, payload);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Food has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.food.status.is.draft");

        publishingUtils.pendingApproveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Food is pending approval.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.food.status.is.draft.approve");

        publishingUtils.approveEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Food has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPublish(entity, "validate.food.status.is.draft.publish");

        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(getPublishSubject(entity), TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Food has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.food.status.is.unpublish");

        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Food has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        FoodEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.food.status.is.draft.unpublish.draft");

        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Food")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Food has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private FoodResponse getResponse(FoodEntity entity) {
        List<FoodCategoryResponse> categories =
                foodCategoryRepository.findFoodCategoriesOfFoodId(entity.getId());


        String imageUrl;
        try {
            imageUrl = minioService.getPreSignedUrl(entity.getImageUrl());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

        return new FoodResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                imageUrl,
                entity.getPrice(),
                entity.getDiscount(),
                entity.getStockQuantity(),
                categories,
                entity.getStatus(),
                entity.getIsDelete(),
                entity.getCreatedByName(),
                entity.getCreatedAt(),
                entity.getUpdatedByName(),
                entity.getUpdatedAt(),
                entity.getDeletionReason(),
                entity.getRejectionReason(),
                entity.getUnpublishReason()
        );
    }

    private FoodPublishResponse getPublishSubject(FoodEntity entity) {
        List<FoodCategoryResponse> categories =
                foodCategoryRepository.findFoodCategoriesOfFoodId(entity.getId());

        return new FoodPublishResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getPrice(),
                entity.getDiscount(),
                entity.getStockQuantity(),
                categories
        );
    }

    private void validateCategories(List<FoodCategoryEntity> categories, Set<UUID> categoryIds) {
        if (categories.size() < categoryIds.size())
            throw new AppBadRequestException("categoryIds", getMessage("validate.food.category.not.exist"));

        categories.forEach(category -> {
            if (category.getStatus() != ContentStatus.PUBLISHED)
                throw new AppBadRequestException("categoryIds", getMessage("validate.food.category.not.exist"));
        });
    }
}
