package vn.tts.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.CategoryBestFoodEntity;
import vn.tts.model.response.category.CategoryBestFoodResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryBestFoodRepository extends JpaRepository<CategoryBestFoodEntity, UUID> {
    @Query("""
            SELECT NEW vn.tts.model.response.category.CategoryBestFoodResponse(
                cbf.id, cbf.categoryPageId, cbf.foodId, cbf.description, f.name, f.description, f.imageUrl
            )
            FROM CategoryBestFoodEntity cbf
            JOIN FoodEntity f ON cbf.foodId = f.id
            WHERE cbf.categoryPageId = :categoryPageId
            ORDER BY cbf.publicationDate DESC
            LIMIT 12
            """)
    List<CategoryBestFoodResponse> getResponsesByCategoryPageId(@Param("categoryPageId") UUID categoryPageId);
}
