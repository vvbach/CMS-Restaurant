package vn.tts.event.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.category.CategoryPageEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.category.CategoryPageRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryPageConsumer implements BaseKafkaConsumer<CategoryPageEntity> {
    private final CategoryPageRepository categoryPageRepository;
    private final KafkaEntityUtils<CategoryPageEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_category_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_page_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "category_page", allEntries = true)
    @Transactional
    public void onPublish(CategoryPageEntity event) {
        kafkaEntityUtils.saveEntity(event, categoryPageRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "category_page", allEntries = true)
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, categoryPageRepository);
    }
}
