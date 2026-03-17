package vn.tts.event.food;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.food.FoodCategoryRelation;
import vn.tts.entity.food.FoodEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.model.response.FoodPublishResponse;
import vn.tts.repository.food.FoodCategoryRelationRepository;
import vn.tts.repository.food.FoodRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodConsumer implements BaseKafkaConsumer<FoodPublishResponse> {
    private final static String TOPIC_PUBLISH = "topic_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_food_unpublish";
    private final FoodRepository foodRepository;
    private final FoodCategoryRelationRepository foodCategoryRelationRepository;

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    public void onPublish(FoodPublishResponse event) {
        try {
            FoodEntity entity = new FoodEntity();
            entity.setId(event.getId());
            entity.setName(event.getName());
            entity.setPrice(event.getPrice());
            entity.setDescription(event.getDescription());
            entity.setImageUrl(event.getImageUrl());
            entity.setDiscount(event.getDiscount());
            entity.setStockQuantity(event.getStockQuantity());

            foodRepository.save(entity);

            List<FoodCategoryRelation> relations = event.getCategories()
                    .stream().map(category -> FoodCategoryRelation.builder()
                            .foodId(entity.getId())
                            .foodCategoryId(category.getId())
                            .build())
                    .toList();

            foodCategoryRelationRepository.saveAll(relations);
        } catch (Exception e) {
            log.error("Error while saving entity: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = {"home_best_food", "home_main_banner"}, allEntries = true)
    public void onUnpublish(UUID id) {
        try {
            foodCategoryRelationRepository.deleteByFoodId(id);
            foodRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error while deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
