package vn.tts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.ContactInfoEntity;

import java.util.Optional;
import java.util.UUID;

public interface ContactInfoRepository extends JpaRepository<ContactInfoEntity, UUID> {
    @Query("""
        SELECT e
        FROM ContactInfoEntity e
        ORDER BY e.publicationDate DESC
        LIMIT 1
    """)
    Optional<ContactInfoEntity> findByLatestPublicationDate();
}
