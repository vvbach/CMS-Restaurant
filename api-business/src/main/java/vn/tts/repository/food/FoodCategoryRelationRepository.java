package vn.tts.repository.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.food.FoodCategoryRelation;

import java.util.List;
import java.util.UUID;

public interface FoodCategoryRelationRepository extends JpaRepository<FoodCategoryRelation, UUID> {
    @Query(value = """
            SELECT r
            FROM FoodCategoryRelation r
            WHERE r.foodId IN :foodIds
            """)
    List<FoodCategoryRelation> findAllByFoodIds(@Param("foodIds") List<UUID> foodIds);

    void deleteByFoodId(UUID foodId);
    void deleteByFoodCategoryId(UUID foodCategoryId);
}
