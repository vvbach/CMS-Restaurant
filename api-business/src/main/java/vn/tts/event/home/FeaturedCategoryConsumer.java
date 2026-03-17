package vn.tts.event.home;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.home.FeaturedCategoryEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.home.FeaturedCategoryRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeaturedCategoryConsumer implements BaseKafkaConsumer<FeaturedCategoryEntity> {
    private final FeaturedCategoryRepository featuredCategoryRepository;
    private final KafkaEntityUtils<FeaturedCategoryEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_featured_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_featured_category_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "featured_category", key = "'featured_category'")
    @Transactional
    public void onPublish(FeaturedCategoryEntity event) {
        kafkaEntityUtils.saveEntity(event, featuredCategoryRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "featured_category", key = "'featured_category'")
    @Transactional
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, featuredCategoryRepository);
    }
}