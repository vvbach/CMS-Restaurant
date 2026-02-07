package vn.tts.service.food;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.food.FoodCategoryRelation;
import vn.tts.entity.food.FoodEntity;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.response.FoodCategoryResponse;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.food.FoodResponse;
import vn.tts.repository.food.FoodCategoryRelationRepository;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.repository.food.FoodRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodCategoryRelationRepository foodCategoryRelationRepository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;


    public PaginationResponse<List<FoodResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, BaseEntity.Fields.publicationDate);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Page<FoodResponse> pageResp = foodRepository.filter(payload, pageable);

        List<FoodCategoryRelation> relations =
                foodCategoryRelationRepository.findAllByFoodIds(
                        pageResp.getContent()
                                .stream().map(FoodResponse::getId)
                                .toList()
                );

        Set<UUID> categoryIds = relations.parallelStream()
                .map(FoodCategoryRelation::getFoodCategoryId)
                .collect(Collectors.toSet());

        Map<UUID, FoodCategoryResponse> categoryMap = foodCategoryRepository.findAllById(categoryIds)
                .parallelStream()
                .collect(Collectors.toMap(
                        BaseEntity::getId,
                        e -> FoodCategoryResponse.builder()
                                .id(e.getId())
                                .name(e.getName())
                                .description(e.getDescription())
                                .build()
                ));

        Map<UUID, List<FoodCategoryResponse>> relationMap = new HashMap<>();

        for (FoodCategoryRelation relation : relations) {
            if (relationMap.get(relation.getFoodId()) == null) {
                List<FoodCategoryResponse> categories = new ArrayList<>();
                categories.add(categoryMap.get(relation.getFoodCategoryId()));
                relationMap.put(relation.getFoodId(), categories);
            } else {
                relationMap.get(relation.getFoodId()).add(categoryMap.get(relation.getFoodCategoryId()));
            }
        }

        pageResp.getContent().parallelStream().forEach(food -> {
            try {
                food.setImageUrl(minioService.getPresignedUrl(food.getImageUrl()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                food.setImageUrl(null);
            }

            food.setCategories(relationMap.get(food.getId()));
        });

        return PaginationResponse.<List<FoodResponse>>builder()
                .total(pageResp.getTotalElements())
                .data(pageResp.getContent())
                .build();
    }

    public FoodResponse getById(UUID id) {
        FoodEntity food = foodRepository.findById(id).orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("food.not.found")));

        try {
            food.setImageUrl(minioService.getPresignedUrl(food.getImageUrl()));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            food.setImageUrl(null);
        }

        List<FoodCategoryRelation> relations = foodCategoryRelationRepository.findAllByFoodIds(List.of(food.getId()));
        if (relations.isEmpty()) {
            return FoodResponse.builder()
                    .id(food.getId())
                    .name(food.getName())
                    .description(food.getDescription())
                    .imageUrl(food.getImageUrl())
                    .price(food.getPrice())
                    .discount(food.getDiscount())
                    .stockQuantity(food.getStockQuantity())
                    .categories(Collections.emptyList())
                    .build();
        }

        Set<UUID> categoryIds = relations.parallelStream()
                .map(FoodCategoryRelation::getFoodCategoryId)
                .collect(Collectors.toSet());

        List<FoodCategoryResponse> categories = foodCategoryRepository.findAllById(categoryIds)
                .parallelStream()
                .map(e -> FoodCategoryResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .description(e.getDescription())
                        .build())
                .toList();

        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .imageUrl(food.getImageUrl())
                .price(food.getPrice())
                .discount(food.getDiscount())
                .stockQuantity(food.getStockQuantity())
                .categories(categories)
                .build();
    }

    public List<FoodResponse> getAll() {
        List<FoodEntity> foods = foodRepository.findAll();

        return getFoodResponses(foods);
    }

    public List<FoodResponse> findByCategoryId(UUID categoryId) {

        List<FoodEntity> foods = foodRepository.findByCategoryId(categoryId);

        return getFoodResponses(foods);
    }

    @NotNull
    private List<FoodResponse> getFoodResponses(List<FoodEntity> foods) {
        List<FoodCategoryRelation> relations =
                foodCategoryRelationRepository.findAllByFoodIds(
                        foods.stream().map(FoodEntity::getId).toList()
                );

        Set<UUID> categoryIds = relations.parallelStream()
                .map(FoodCategoryRelation::getFoodCategoryId)
                .collect(Collectors.toSet());

        Map<UUID, FoodCategoryResponse> categoryMap = foodCategoryRepository.findAllById(categoryIds)
                .parallelStream()
                .collect(Collectors.toMap(
                        BaseEntity::getId,
                        e -> FoodCategoryResponse.builder()
                                .id(e.getId())
                                .name(e.getName())
                                .description(e.getDescription())
                                .build()
                ));

        Map<UUID, List<FoodCategoryResponse>> relationMap = new HashMap<>();

        for (FoodCategoryRelation relation : relations) {
            if (relationMap.get(relation.getFoodId()) == null) {
                List<FoodCategoryResponse> categories = new ArrayList<>();
                categories.add(categoryMap.get(relation.getFoodCategoryId()));
                relationMap.put(relation.getFoodId(), categories);
            } else {
                relationMap.get(relation.getFoodId()).add(categoryMap.get(relation.getFoodCategoryId()));
            }
        }

        return foods.parallelStream().map(food -> {
            String imageUrl = null;
            try {
                imageUrl = minioService.getPresignedUrl(food.getImageUrl());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            return FoodResponse.builder()
                    .id(food.getId())
                    .name(food.getName())
                    .description(food.getDescription())
                    .imageUrl(imageUrl)
                    .price(food.getPrice())
                    .discount(food.getDiscount())
                    .stockQuantity(food.getStockQuantity())
                    .categories(relationMap.getOrDefault(food.getId(), Collections.emptyList()))
                    .build();
        }).collect(Collectors.toList());
    }

}
