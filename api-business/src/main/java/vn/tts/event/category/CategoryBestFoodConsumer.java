package vn.tts.event.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.category.CategoryBestFoodEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.category.CategoryBestFoodProxy;
import vn.tts.repository.category.CategoryBestFoodRepository;
import vn.tts.service.ServiceUtil;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryBestFoodConsumer implements BaseKafkaConsumer<CategoryBestFoodEntity> {
    private final CategoryBestFoodRepository categoryBestFoodRepository;
    private final KafkaEntityUtils<CategoryBestFoodEntity> kafkaEntityUtils;
    private final ServiceUtil serviceUtil;
    private final CategoryBestFoodProxy categoryBestFoodProxy;

    private final static String TOPIC_PUBLISH = "topic_category_best_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_best_food_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "category_best_food", key = "#p0.categoryPageId")
    @Transactional
    public void onPublish(CategoryBestFoodEntity event) {
        kafkaEntityUtils.saveEntity(event, categoryBestFoodRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @Transactional
    public void onUnPublish(UUID id) {
        CategoryBestFoodEntity entity = categoryBestFoodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("category.best.food.not.found")));

        categoryBestFoodProxy.cacheEvictCategoryBestFoodResponses(entity.getCategoryPageId());

        kafkaEntityUtils.deleteEntity(id, categoryBestFoodRepository);
    }
}
