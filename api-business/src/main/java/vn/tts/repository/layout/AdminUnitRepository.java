package vn.tts.repository.layout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.layout.AdminUnitEntity;

import java.util.Optional;
import java.util.UUID;

public interface AdminUnitRepository extends JpaRepository<AdminUnitEntity, UUID> {
    @Query("""
        SELECT e
        FROM AdminUnitEntity e
        ORDER BY e.publicationDate DESC
        LIMIT 1
    """)
    Optional<AdminUnitEntity> findByLatestPublicationDate();
}
