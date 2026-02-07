package vn.tts.event.layout;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.layout.MottoEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.layout.MottoProxy;
import vn.tts.repository.layout.MottoRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MottoConsumer implements BaseKafkaConsumer<MottoEntity> {
    private final MottoRepository mottoRepository;
    private final MottoProxy mottoProxy;
    private final KafkaEntityUtils<MottoEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_motto_publish";
    private final static String TOPIC_UNPUBLISH = "topic_motto_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(MottoEntity event) {
        kafkaEntityUtils.saveEntity(event, mottoRepository);
        mottoProxy.cachePutMottoResponse(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "motto", key = "'motto'")
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, mottoRepository);
    }
}
