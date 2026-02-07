package vn.tts.config.kafka;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Bean
    public JsonSerializer<Object> jsonSerializer() {
        return new JsonSerializer<>();
    }

    @Bean
    public RecordMessageConverter recordMessageConverter() {
        return new StringJsonMessageConverter(); // why: chuyển String JSON -> POJO theo kiểu method param
    }

    @Bean
    public JsonDeserializer<Object> jsonDeserializer() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*"); //add this line for untrusted packages, this is very important
        return deserializer;
    }
}
