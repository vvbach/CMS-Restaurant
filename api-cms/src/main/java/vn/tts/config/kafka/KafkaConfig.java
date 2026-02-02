package vn.tts.config.kafka;


import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${spring.kafka.listener.concurrency}")
    private int concurrency;

    @Bean
    public RecordMessageConverter recordMessageConverter() {
        return new StringJsonMessageConverter();
    }

    @Bean
    public JsonDeserializer<Object> jsonDeserializer() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*"); // hoặc giới hạn package riêng của bạn để bảo mật hơn
        deserializer.setUseTypeMapperForKey(true);
        return deserializer;
    }

    // Bean cấu hình recoverer cho message lỗi
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> template) {
        return new DeadLetterPublishingRecoverer(
                template,
                // Định nghĩa DLT topic bằng cách thêm hậu tố ".DLT" theo topic gốc
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recover, KafkaRetryProperties props) {
        // Delay 1s mỗi lần retry
        return new DefaultErrorHandler(
                recover,
                new FixedBackOff(props.getInitialInterval(), props.getMaxAttempts() - 1)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler) {
        var listenerFactory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        listenerFactory.setConcurrency(concurrency);
        listenerFactory.setConsumerFactory(consumerFactory);
        listenerFactory.setRecordMessageConverter(recordMessageConverter());
        listenerFactory.setCommonErrorHandler(errorHandler);
        return listenerFactory;
    }
}
