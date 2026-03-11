package vn.tts.repository.food;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tts.entity.food.FoodCategoryEntity;
import vn.tts.model.dto.FoodCategoryDto;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.response.food.FoodCategoryResponse;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface FoodCategoryRepository extends
        JpaRepository<FoodCategoryEntity, UUID>,
        RevisionRepository<FoodCategoryEntity, UUID, Integer> {
    @NotNull
    @Query("""
                SELECT c
                FROM FoodCategoryEntity c
                WHERE c.id IN :ids
            """)
    List<FoodCategoryEntity> findAllById(@NotNull Iterable<UUID> ids);

    @Query(value = """
            
              SELECT EXISTS (
                SELECT 1
                FROM FoodCategoryEntity c
                WHERE c.name = ?#{#name}
            )
            """)
    Boolean existsByName(String name);

    @Query("""
             SELECT DISTINCT NEW vn.tts.model.response.food.FoodCategoryResponse(
                 fc.id,
                 fc.name,
                 fc.description
             )
             FROM FoodCategoryEntity fc
             WHERE EXISTS (
                 SELECT 1
                 FROM FoodCategoryRelation r
                 WHERE r.foodId = :foodId
                 AND r.foodCategoryId = fc.id
                 AND fc.isDelete = 0
                 AND r.isDelete = 0
                 AND fc.status = 'PUBLISHED'
             )
            """)
    List<FoodCategoryResponse> findFoodCategoriesOfFoodId(@Param("foodId") UUID foodId);

    @Query(value = """
             SELECT c.id, c.name, c.description, c.status,
                    c.is_delete, c.created_by_name, c.created_at, c.updated_by_name, c.updated_at,
                    c.reason_delete, c.reason_rejection, c.reason_unpublish, rel.rev
             FROM public.food_category_relation_aud rel
             JOIN public.food_category c ON rel.food_category_id = c.id
             WHERE rel.rev IN (:revs)
            """, nativeQuery = true)
    List<FoodCategoryDto> findFoodCategoriesByRevNumbers(@Param("revs") Collection<Integer> revs);

    @Query("""
                select f from FoodCategoryEntity f where
                (:#{#payload.description} is null
                    or f.description like concat(:#{#payload.description}, '%')
                    or f.name like concat(:#{#payload.description}, '%')
                )
                and (:#{#payload.isDelete} is null or f.isDelete = :#{#payload.isDelete})
                and (:#{#payload.status} is null or f.status = :#{#payload.status})
                and (CAST(:fromDate AS timestamp) is null or f.createdAt >= :fromDate)
                and (CAST(:toDate AS timestamp) is null or f.createdAt < :toDate)
            """)
    Page<FoodCategoryEntity> filter(@Param("payload") FilterPayload payload,
                                    @Param("fromDate") OffsetDateTime fromDate,
                                    @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
