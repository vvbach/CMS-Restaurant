package vn.tts.repository.home;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.home.HomeMainBannerEntity;
import vn.tts.model.response.home.HomeMainBannerResponse;

import java.util.List;
import java.util.UUID;

public interface HomeMainBannerRepository extends
        JpaRepository<HomeMainBannerEntity, UUID> {
    @Query(value = """
    SELECT e.id, e.food_id, e.title, e.description, e.image_url
    FROM public.home_main_banner e
    WHERE EXISTS (
        SELECT f.id
        FROM public.food f
        WHERE f.id = e.food_id
    )
    ORDER BY e.publication_date DESC
    LIMIT 3
    """, nativeQuery = true)
    List<HomeMainBannerResponse> getHomeMainBannerResponses();

    void deleteByFoodId(UUID foodId);
}
