package vn.tts.repository.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.food.FoodEntity;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.response.food.FoodResponse;
import vn.tts.model.response.layout.BannerFooterResponse;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<FoodEntity, UUID> {
    @Query("""
    SELECT NEW vn.tts.model.response.food.FoodResponse(
        f.id,
        f.name,
        f.description,
        f.imageUrl,
        f.price,
        f.discount,
        f.stockQuantity
    )
    FROM FoodEntity f
    JOIN FoodCategoryRelation fcr ON f.id = fcr.foodId
    JOIN FoodCategoryEntity fc ON fcr.foodCategoryId = fc.id
    WHERE
    (:#{#payload.categoryName} IS NULL OR fc.name = :#{#payload.categoryName})
    AND (:#{#payload.minPrice} IS NULL OR f.price >= :#{#payload.minPrice})
    AND (:#{#payload.maxPrice} IS NULL OR f.price <= :#{#payload.maxPrice})
    AND (:#{#payload.searchQuery} IS NULL OR
         LOWER(f.name) LIKE LOWER(CONCAT('%', :#{#payload.searchQuery}, '%')) OR
         LOWER(f.description) LIKE LOWER(CONCAT('%', :#{#payload.searchQuery}, '%')))
    GROUP BY f.id, f.name, f.description, f.imageUrl, f.price, f.discount, f.stockQuantity
    """)
    Page<FoodResponse> filter(FilterPayload payload, Pageable pageable);

    @Query("""
        SELECT f
        FROM FoodEntity f
        JOIN FoodCategoryRelation fcr ON f.id = fcr.foodId
        JOIN FoodCategoryEntity fc ON fcr.foodCategoryId = :categoryId
    """)
    List<FoodEntity> findByCategoryId(UUID categoryId);

    @Query(value = """
            WITH each_from_category AS (
            	SELECT f.id, f.image_url
            	FROM public.food f
            	JOIN (
            		SELECT r.food_id,
            		ROW_NUMBER() OVER (PARTITION BY r.food_id ORDER BY r.food_category_id) AS rn
            		FROM public.food_category_relation r
            	) t ON f.id = t.food_id
            	WHERE rn = 1
            	ORDER BY f.publication_date DESC
            	LIMIT :quantity
            ),
            other_foods AS (
            	SELECT f.id, f.image_url
            	FROM public.food f
            	WHERE f.id NOT IN (SELECT ffc.id FROM each_from_category ffc)
            	LIMIT :quantity - (SELECT COUNT(*) FROM each_from_category)
            )
            
            SELECT *
            FROM each_from_category
            UNION ALL
            SELECT *
            FROM other_foods
            """, nativeQuery = true)
    List<BannerFooterResponse> getBannerFooterResponses(@Param("quantity") int quantity);
}
