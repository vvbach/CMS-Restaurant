package vn.tts.event.home;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.home.HomeBestFoodEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.home.HomeBestFoodRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeBestFoodConsumer implements BaseKafkaConsumer<HomeBestFoodEntity> {
    private final HomeBestFoodRepository homeBestFoodRepository;
    private final KafkaEntityUtils<HomeBestFoodEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_home_best_food_publish";
    private final static String TOPIC_UNPUBLISH = "topic_home_best_food_unpublish";


    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "home_best_food", key = "'home_best_food'")
    @Transactional
    public void onPublish(HomeBestFoodEntity event) {
        kafkaEntityUtils.saveEntity(event, homeBestFoodRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "home_best_food", key = "'home_best_food'")
    @Transactional
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, homeBestFoodRepository);
    }
}
