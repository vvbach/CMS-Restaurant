package vn.tts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tts.entity.SystemConfigEntity;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, String> {
}
