package vn.tts.event.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.category.AboutCategoryEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.category.AboutCategoryProxy;
import vn.tts.repository.category.AboutCategoryRepository;
import vn.tts.service.ServiceUtil;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AboutCategoryConsumer implements BaseKafkaConsumer<AboutCategoryEntity> {
    private final AboutCategoryRepository aboutCategoryRepository;
    private final KafkaEntityUtils<AboutCategoryEntity> kafkaEntityUtils;
    private final AboutCategoryProxy aboutCategoryProxy;
    private final ServiceUtil serviceUtil;

    private final static String TOPIC_PUBLISH = "topic_about_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_about_category_unpublish";

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    public void onPublish(AboutCategoryEntity event) {
        kafkaEntityUtils.saveEntity(event, aboutCategoryRepository);
        aboutCategoryProxy.cachePutAboutCategoryResponse(event);
    }

    @Override
    @Transactional
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    public void onUnpublish(UUID id) {
        AboutCategoryEntity entity = aboutCategoryRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("about.category.not.found")));

        aboutCategoryProxy.cacheEvictAboutCategoryResponse(entity.getCategoryPageId());

        kafkaEntityUtils.deleteEntity(id, aboutCategoryRepository);
    }
}
