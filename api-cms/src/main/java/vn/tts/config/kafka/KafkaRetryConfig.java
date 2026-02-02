package vn.tts.config.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaRetryProperties.class)
public class KafkaRetryConfig {
}
