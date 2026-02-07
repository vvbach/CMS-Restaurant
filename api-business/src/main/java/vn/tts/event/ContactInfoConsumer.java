package vn.tts.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.ContactInfoEntity;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.ContactInfoProxy;
import vn.tts.repository.ContactInfoRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactInfoConsumer implements BaseKafkaConsumer<ContactInfoEntity> {
    private final ContactInfoRepository contactInfoRepository;
    private final ContactInfoProxy contactInfoProxy;
    private final KafkaEntityUtils<ContactInfoEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_contact_info_publish";
    private final static String TOPIC_UNPUBLISH = "topic_contact_info_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(ContactInfoEntity event) {
        kafkaEntityUtils.saveEntity(event, contactInfoRepository);
        contactInfoProxy.cachePutContactInfoResponse(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "contact_info", key = "'contact_info'")
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, contactInfoRepository);
    }
}