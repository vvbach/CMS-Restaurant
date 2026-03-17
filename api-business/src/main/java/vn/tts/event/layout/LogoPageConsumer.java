package vn.tts.event.layout;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.layout.LogoPageEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.layout.LogoPageProxy;
import vn.tts.repository.layout.LogoPageRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoPageConsumer implements BaseKafkaConsumer<LogoPageEntity> {
    private final LogoPageRepository repository;
    private final LogoPageProxy logoPageProxy;
    private final KafkaEntityUtils<LogoPageEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_logo_page_publish";
    private final static String TOPIC_UNPUBLISH = "topic_logo_page_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(LogoPageEntity event) {
        kafkaEntityUtils.saveEntity(event, repository);
        logoPageProxy.cachePutLogoPageResponse(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "logo_page", key = "'logo_page'")
    @Transactional
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, repository);
    }
}
