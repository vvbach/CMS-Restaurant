package vn.tts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tts.entity.ImageWebEntity;

import java.util.UUID;

public interface ImageWebRepository extends JpaRepository<ImageWebEntity, UUID> {
}
