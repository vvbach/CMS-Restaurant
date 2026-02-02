package vn.tts.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.listener.retry")
@Data
public class KafkaRetryProperties {
    private int maxAttempts;
    private long initialInterval;
    private double multiplier;
    private long maxInterval;
}
