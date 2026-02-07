package vn.tts.event;

import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

public interface BaseKafkaConsumer<T> {
    @KafkaListener
    void onPublish(T event);
    @KafkaListener
    void onUnPublish(UUID id);
}
