package vn.tts.event.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import vn.tts.entity.BaseEntity;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaEntityUtils<E extends BaseEntity> {
    public void saveEntity(E entity, JpaRepository<E, UUID> repository) {
        try {
            repository.save(entity);
        } catch (Exception e) {
            log.error("Error while saving entity: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteEntity(UUID id, JpaRepository<E, UUID> repository) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            log.error("Error while deleting entity: {}", e.getMessage(), e);
            throw e;
        }
    }
}
