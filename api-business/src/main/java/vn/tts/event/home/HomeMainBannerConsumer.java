package vn.tts.event.home;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.home.HomeMainBannerEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.repository.home.HomeMainBannerRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeMainBannerConsumer implements BaseKafkaConsumer<HomeMainBannerEntity> {
    private final HomeMainBannerRepository homeMainBannerRepository;
    private final KafkaEntityUtils<HomeMainBannerEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_home_main_banner_publish";
    private final static String TOPIC_UNPUBLISH = "topic_home_main_banner_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @CacheEvict(cacheNames = "home_main_banner", key = "'home_main_banner'")
    @Transactional
    public void onPublish(HomeMainBannerEntity event) {
        kafkaEntityUtils.saveEntity(event, homeMainBannerRepository);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "home_main_banner", key = "'home_main_banner'")
    @Transactional
    public void onUnPublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, homeMainBannerRepository);
    }
}
