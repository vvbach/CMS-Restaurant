package vn.tts.repository.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tts.entity.food.FoodCategoryRelation;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodCategoryRelationRepository extends
        JpaRepository<FoodCategoryRelation, UUID>,
        RevisionRepository<FoodCategoryRelation, UUID, Integer> {

    @Query(value = """
            SELECT r
            FROM FoodCategoryRelation r
            WHERE r.foodId = :foodId
            AND r.isDelete = 0
            """)
    List<FoodCategoryRelation> findAllByFoodId(@Param("foodId") UUID foodId);

    @Modifying
    @Query(value = """
            UPDATE FoodCategoryRelation rel
            SET rel.isDelete = 1
            WHERE rel.foodCategoryId = :foodCategoryId
            """)
    void deleteAllByFoodCategoryId(@Param("foodCategoryId") UUID foodCategoryId);

    @Modifying
    @Query(value = """
        UPDATE FoodCategoryRelation rel
        SET rel.isDelete = 1
        WHERE rel.foodId = :foodId
    """)
    void deleteByFoodId(@Param("foodId") UUID foodId);
}
