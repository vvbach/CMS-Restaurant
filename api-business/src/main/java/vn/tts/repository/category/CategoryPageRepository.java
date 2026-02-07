package vn.tts.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.CategoryPageEntity;
import vn.tts.model.response.category.CategoryPageResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryPageRepository extends JpaRepository<CategoryPageEntity, UUID> {
    @Query("""
        SELECT NEW vn.tts.model.response.category.CategoryPageResponse(
            e.id, e.categoryId, fc.name
        )
        FROM CategoryPageEntity e
        JOIN FoodCategoryEntity fc ON e.categoryId = fc.id
        ORDER BY e.publicationDate DESC
        LIMIT :quantity
    """)
    List<CategoryPageResponse> getCategoryPageResponses(@Param("quantity") int quantity);

    void deleteByCategoryId(UUID categoryId);
}
