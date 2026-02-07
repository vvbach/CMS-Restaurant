package vn.tts.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.CategoryMainBannerEntity;
import vn.tts.model.response.category.CategoryMainBannerResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryMainBannerRepository extends
        JpaRepository<CategoryMainBannerEntity, UUID> {

    @Query("""
    SELECT NEW vn.tts.model.response.category.CategoryMainBannerResponse(
        e.id, e.foodId, e.title, e.description, e.imageUrl
    )
    FROM CategoryMainBannerEntity e
    WHERE e.categoryPageId = :categoryPageId
    AND EXISTS (
        SELECT f.id
        FROM FoodEntity f
        WHERE f.id = e.foodId
    )
    ORDER BY e.publicationDate DESC
    LIMIT 3
    """)
    List<CategoryMainBannerResponse> getResponsesByCategoryPageId(@Param("categoryPageId") UUID categoryPageId);
}
