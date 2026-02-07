package vn.tts.repository.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.model.response.FoodCategoryResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodCategoryRepository extends JpaRepository<FoodCategoryEntity, UUID> {
    @Query("""
        SELECT NEW vn.tts.model.response.FoodCategoryResponse(
            e.id, e.name, e.description
        ) FROM FoodCategoryEntity e
        WHERE e.id = :id
    """)
    Optional<FoodCategoryResponse> getResponseById(@Param("id") UUID id);

    @Query("""
        SELECT NEW vn.tts.model.response.FoodCategoryResponse(
            e.id,
            e.name,
            e.description
        )
        FROM FoodCategoryEntity e
    """)
    List<FoodCategoryResponse> getCategories();

    @Query("""
            SELECT NEW vn.tts.model.response.FoodCategoryResponse(
                fc.id,
                fc.name,
                fc.description
            )
            FROM FoodCategoryEntity fc
            WHERE EXISTS (
                SELECT fc.id
                FROM FoodCategoryRelation r
                WHERE r.foodId = :foodId
                AND r.foodCategoryId = fc.id
            )
            """)
    List<FoodCategoryResponse> getCategoryResponsesOfFood(@Param("foodId") UUID foodId);
}
