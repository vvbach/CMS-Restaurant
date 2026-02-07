package vn.tts.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.CategoryStatisticEntity;
import vn.tts.model.response.category.CategoryStatisticResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryStatisticRepository extends JpaRepository<CategoryStatisticEntity, UUID> {
    @Query("""
            WITH tmp AS (
                 SELECT DISTINCT rel.foodId AS foodId
                 FROM FoodCategoryRelation rel
                 JOIN CategoryPageEntity cp ON cp.categoryId = rel.foodCategoryId
                 WHERE cp.id = :categoryPageId
            )
            
            SELECT NEW vn.tts.model.response.category.CategoryStatisticResponse(
                cs.id, cs.categoryId, CAST(COUNT(t.foodId) AS integer), cs.name, cs.description, cs.imageUrl
            )
            FROM CategoryStatisticEntity cs, tmp t
            JOIN FoodCategoryRelation r
            ON r.foodId = t.foodId
            WHERE r.foodCategoryId = cs.categoryId
            GROUP BY cs.id, cs.categoryId
            ORDER BY cs.publicationDate DESC
            LIMIT :quantity
            """)
    List<CategoryStatisticResponse> getCategoryStatisticResponsesByCategoryPageId(
            @Param("categoryPageId") UUID categoryPageId,
            @Param("quantity") Integer quantity
    );
}
