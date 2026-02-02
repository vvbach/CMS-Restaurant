package vn.tts.repository.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tts.entity.food.FoodEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends
        JpaRepository<FoodEntity, UUID>,
        RevisionRepository<FoodEntity, UUID, Integer> {
    List<FoodEntity> findAllByIdIn(Collection<UUID> id);

    @Query(value = """
            SELECT distinct f FROM FoodEntity f
            WHERE EXISTS (
                SELECT 1
                FROM FoodCategoryRelation r
                WHERE r.foodId = f.id
                AND r.foodCategoryId = :categoryId
            )
            """)
    List<FoodEntity> findAllByCategoryId(@Param("categoryId") UUID categoryId);

    @Query(value = """
            
            select distinct f from FoodEntity f
            where exists (
              select 1
              from FoodCategoryRelation r
              join FoodCategoryEntity fc on fc.id = r.foodCategoryId
              where r.foodId = f.id
              and fc.name = :categoryName
            )
            """)
    List<FoodEntity> findAllByCategoryName(@Param("categoryName") String categoryName);

    @Query("""
        select f from FoodEntity f
        join FoodCategoryRelation fcr on fcr.foodId = f.id
        join FoodCategoryEntity fc on fcr.foodCategoryId = fc.id
        where
        (:#{#payload.description} is null
            or f.description like concat(:#{#payload.description}, '%')
            or f.name like concat(:#{#payload.description}, '%')
        )
        and (:#{#payload.categoryName} is null or fc.name like concat(:#{#payload.categoryName}, '%'))
        and (:#{#payload.isDelete} is null or f.isDelete = :#{#payload.isDelete})
        and (:#{#payload.status} is null or f.status = :#{#payload.status})
        and (CAST(:fromDate AS timestamp) is null or f.createdAt >= :fromDate)
        and (CAST(:toDate AS timestamp) is null or f.createdAt < :toDate)
    """)
    Page<FoodEntity> filter(FilterPayload payload,
                            @Param("fromDate") OffsetDateTime fromDate,
                            @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
