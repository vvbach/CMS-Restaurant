package vn.tts.repository.layout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.layout.MottoEntity;

import java.util.Optional;
import java.util.UUID;

public interface MottoRepository extends JpaRepository<MottoEntity, UUID> {
    @Query("""
        SELECT e
        FROM MottoEntity e
        ORDER BY e.publicationDate DESC
        LIMIT 1
    """)
    Optional<MottoEntity> findByLatestPublicationDate();
}
