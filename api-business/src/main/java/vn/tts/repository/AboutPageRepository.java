package vn.tts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.AboutPageEntity;

import java.util.Optional;
import java.util.UUID;

public interface AboutPageRepository extends JpaRepository<AboutPageEntity, UUID> {
    @Query("""
        SELECT e
        FROM AboutPageEntity e
        ORDER BY e.publicationDate DESC
        LIMIT 1
    """)
    Optional<AboutPageEntity> findByLatestPublicationDate();
}
