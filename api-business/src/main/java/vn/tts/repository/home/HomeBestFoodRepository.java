package vn.tts.repository.home;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.home.HomeBestFoodEntity;
import vn.tts.model.response.home.HomeBestFoodResponse;

import java.util.List;
import java.util.UUID;

public interface HomeBestFoodRepository extends JpaRepository<HomeBestFoodEntity, UUID> {
    @Query(value = """
            SELECT hbf.id, f.id, hbf.description, f.name, f.description, f.image_url
            FROM public.home_best_food hbf
            JOIN public.food f ON hbf.food_id = f.id
            ORDER BY hbf.publication_date DESC
            LIMIT 12
            """, nativeQuery = true)
    List<HomeBestFoodResponse> getHomeBestFoodResponses();

    void deleteByFoodId(UUID foodId);
}
