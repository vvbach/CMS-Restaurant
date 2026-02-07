package vn.tts.event.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.category.CategoryMainBannerEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.category.CategoryMainBannerProxy;
import vn.tts.repository.category.CategoryMainBannerRepository;
import vn.tts.service.ServiceUtil;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryMainBannerConsumer implements BaseKafkaConsumer<CategoryMainBannerEntity> {
    private final CategoryMainBannerRepository categoryMainBannerRepository;
    private final KafkaEntityUtils<CategoryMainBannerEntity> kafkaEntityUtils;
    private final CategoryMainBannerProxy categoryMainBannerProxy;
    private final ServiceUtil serviceUtil;

    private final static String TOPIC_PUBLISH = "topic_category_main_banner_publish";
    private final static String TOPIC_UNPUBLISH = "topic_category_main_banner_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "category_main_banner", key = "#p0.categoryPageId")
    @Transactional
    public void onPublish(CategoryMainBannerEntity event) {
        kafkaEntityUtils.saveEntity(event, categoryMainBannerRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @Transactional
    public void onUnPublish(UUID id) {
        CategoryMainBannerEntity entity = categoryMainBannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("category.main.banner.not.found")));

        categoryMainBannerProxy.cacheEvictCategoryMainBannerResponse(entity.getCategoryPageId());

        kafkaEntityUtils.deleteEntity(id, categoryMainBannerRepository);
    }
}
