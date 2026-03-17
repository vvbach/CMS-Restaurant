package vn.tts.event.layout;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.entity.layout.AdminUnitEntity;
import vn.tts.event.BaseKafkaConsumer;
import vn.tts.event.utils.KafkaEntityUtils;
import vn.tts.proxy.layout.AdminUnitProxy;
import vn.tts.repository.layout.AdminUnitRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUnitConsumer implements BaseKafkaConsumer<AdminUnitEntity> {
    private final AdminUnitRepository adminUnitRepository;
    private final AdminUnitProxy adminUnitProxy;
    private final KafkaEntityUtils<AdminUnitEntity> kafkaEntityUtils;

    private final static String TOPIC_PUBLISH = "topic_admin_unit_publish";
    private final static String TOPIC_UNPUBLISH = "topic_admin_unit_unpublish";

    @Override
    @KafkaListener(topics = TOPIC_PUBLISH, groupId = TOPIC_PUBLISH)
    @Transactional
    public void onPublish(AdminUnitEntity event) {
        kafkaEntityUtils.saveEntity(event, adminUnitRepository);
        adminUnitProxy.cachePutAdminUnitResponse(event);
    }

    @Override
    @KafkaListener(topics = TOPIC_UNPUBLISH, groupId = TOPIC_UNPUBLISH)
    @CacheEvict(cacheNames = "admin_unit", key = "'admin_unit'")
    @Transactional
    public void onUnpublish(UUID id) {
        kafkaEntityUtils.deleteEntity(id, adminUnitRepository);
    }
}
