package vn.tts.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.ImageWebEntity;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.ImageWebRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageWebConsumer implements BaseKafkaConsumer<ImageWebEntity> {
    private final ImageWebRepository imageWebRepository;
    private final KafkaEntityUtils<ImageWebEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_image_publish";
    private final static String TOPIC_UNPUBLISH = "topic_image_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(ImageWebEntity event) {
        kafkaEntityUtils.saveEntity(event, imageWebRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @Transactional
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, imageWebRepository);
    }
}