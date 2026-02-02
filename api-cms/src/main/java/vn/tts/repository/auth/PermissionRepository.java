package vn.tts.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import vn.tts.entity.auth.PermissionEntity;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends
        JpaRepository<PermissionEntity, UUID>,
        RevisionRepository<PermissionEntity, UUID, Integer> {
    Optional<PermissionEntity> findByCode(String code);
}
