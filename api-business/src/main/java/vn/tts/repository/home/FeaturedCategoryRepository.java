package vn.tts.repository.home;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.home.FeaturedCategoryEntity;
import vn.tts.model.response.home.FeaturedCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface FeaturedCategoryRepository extends JpaRepository<FeaturedCategoryEntity, UUID> {
    @Query(value = """
            SELECT fe.id, fe.category_id, fo.name, fe.description, fe.image_url
            FROM public.featured_category fe
            JOIN public.food_categories fo ON fe.category_id = fo.id
            ORDER BY fe.publication_date DESC
            LIMIT 2
            """, nativeQuery = true)
    List<FeaturedCategoryResponse> getFeaturedCategoryResponses();

    void deleteByCategoryId(UUID categoryId);
}
