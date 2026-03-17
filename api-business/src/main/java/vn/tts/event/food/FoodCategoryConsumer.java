package vn.tts.event.food;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.food.FoodCategoryRelationRepository;
import vn.tts.repository.food.FoodCategoryRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FoodCategoryConsumer implements BaseKafkaConsumer<FoodCategoryEntity> {
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodCategoryRelationRepository foodCategoryRelationRepository;
    private final KafkaEntityUtils<FoodCategoryEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_food_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_food_category_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(FoodCategoryEntity event) {
        kafkaEntityUtils.saveEntity(event, foodCategoryRepository);
    }

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = {"category_page", "featured_category"}, allEntries = true)
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, foodCategoryRepository);

        try {
            foodCategoryRelationRepository.deleteByFoodCategoryId(id);
        } catch (Exception e) {
            log.error("Error while deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}