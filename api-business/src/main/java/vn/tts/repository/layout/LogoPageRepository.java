package vn.tts.repository.layout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.layout.LogoPageEntity;

import java.util.Optional;
import java.util.UUID;

public interface LogoPageRepository extends JpaRepository<LogoPageEntity, UUID> {
    @Query("""
        SELECT e
        FROM LogoPageEntity e
        ORDER BY e.publicationDate DESC
        LIMIT 1
    """)
    Optional<LogoPageEntity> findByLatestPublicationDate();
}
