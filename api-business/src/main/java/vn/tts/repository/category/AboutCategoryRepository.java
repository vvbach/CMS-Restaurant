package vn.tts.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.AboutCategoryEntity;

import java.util.UUID;

public interface AboutCategoryRepository extends JpaRepository<AboutCategoryEntity, UUID> {
    @Query("""
            SELECT e
            FROM AboutCategoryEntity e
            WHERE e.categoryPageId = :categoryPageId
            ORDER BY e.publicationDate DESC
            LIMIT 1
            """)
    AboutCategoryEntity getLatestByCategoryPageId(@Param("categoryPageId") UUID categoryPageId);
}
