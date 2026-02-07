package vn.tts.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.AboutPageEntity;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.AboutPageProxy;
import vn.tts.repository.AboutPageRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AboutPageConsumer implements BaseKafkaConsumer<AboutPageEntity> {
    private final AboutPageRepository aboutPageRepository;
    private final AboutPageProxy aboutPageProxy;
    private final KafkaEntityUtils<AboutPageEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_about_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_about_page_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(AboutPageEntity event) {
        kafkaEntityUtils.saveEntity(event, aboutPageRepository);
        aboutPageProxy.cachePutAboutPageResponse(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "about_page", key = "'about_page'")
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, aboutPageRepository);
    }
}
