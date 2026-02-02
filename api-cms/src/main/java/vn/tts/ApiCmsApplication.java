package vn.tts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class ApiCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiCmsApplication.class, args);
    }
}