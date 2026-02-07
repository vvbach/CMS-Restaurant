package vn.tts.event.layout;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.layout.SocialLinkEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.layout.SocialLinkProxy;
import vn.tts.repository.layout.SocialLinkRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocialLinkConsumer implements BaseKafkaConsumer<SocialLinkEntity> {
    private final SocialLinkRepository socialLinkRepository;
    private final SocialLinkProxy socialLinkProxy;
    private final KafkaEntityUtils<SocialLinkEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_social_link_publish";
    private final static String TOPIC_UNPUBLISH = "topic_social_link_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(SocialLinkEntity event) {
        kafkaEntityUtils.saveEntity(event, socialLinkRepository);
        socialLinkProxy.cachePutSocialLinkResponses(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "social_link", key = "'social_link'")
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, socialLinkRepository);
    }
}
