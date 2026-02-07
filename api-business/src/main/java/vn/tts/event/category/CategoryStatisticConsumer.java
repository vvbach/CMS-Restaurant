package vn.tts.event.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.category.CategoryStatisticEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.category.CategoryStatisticProxy;
import vn.tts.repository.category.CategoryStatisticRepository;
import vn.tts.service.ServiceUtil;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryStatisticConsumer implements BaseKafkaConsumer<CategoryStatisticEntity> {
    private final CategoryStatisticRepository categoryStatisticRepository;
    private final KafkaEntityUtils<CategoryStatisticEntity> kafkaEntityUtils;
    private final CategoryStatisticProxy categoryStatisticProxy;
    private final ServiceUtil serviceUtil;

    private final static String TOPIC_PUBLISH = "topic_category_statistic_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_statistic_unpublish";

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "category_statistic", key = "#p0.categoryPageId")
    public void onPublish(CategoryStatisticEntity event) {
        kafkaEntityUtils.saveEntity(event, categoryStatisticRepository);
    }

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    public void onUnPublish(UUID id) {
        CategoryStatisticEntity entity = categoryStatisticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("category.statistic.not.found")));

        categoryStatisticProxy.cacheEvictCategoryStatisticResponses(entity.getCategoryPageId());

        kafkaEntityUtils.deleteEntity(id, categoryStatisticRepository);
    }
}
