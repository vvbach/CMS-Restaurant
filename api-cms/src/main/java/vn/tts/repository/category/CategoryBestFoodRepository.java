package vn.tts.repository.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.category.CategoryBestFoodEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CategoryBestFoodRepository extends
        JpaRepository<CategoryBestFoodEntity, UUID>,
        RevisionRepository<CategoryBestFoodEntity, UUID, Integer> {
    @Query("""
            SELECT e FROM CategoryBestFoodEntity e WHERE
            (:categoryPageId IS NULL OR e.categoryPageId = :categoryPageId)
            AND (:#{#payload.description} IS NULL OR e.description LIKE CONCAT('%', :#{#payload.description}, '%'))
            AND (:#{#payload.createdByName} IS NULL OR e.createdByName LIKE CONCAT('%', :#{#payload.createdByName}, '%'))
            AND (:#{#payload.isDelete} IS NULL OR e.isDelete = :#{#payload.isDelete})
            AND (:#{#payload.status} IS NULL OR e.status = :#{#payload.status})
            AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR e.createdAt >= :fromDate)
            AND (CAST(:toDate AS TIMESTAMP) IS NULL OR e.createdAt < :toDate)
            """)
    Page<CategoryBestFoodEntity> filter(FilterPayload payload,
                                        @Param("categoryPageId") UUID categoryPageId,
                                        @Param("fromDate") OffsetDateTime fromDate,
                                        @Param("toDate") OffsetDateTime toDate,
                                        Pageable pageable);

    @Query("""
            SELECT e
            FROM CategoryBestFoodEntity e
            WHERE e.categoryPageId = :categoryPageId
            """)
    List<CategoryBestFoodEntity> findAllByCategoryPageId(@Param("categoryPageId") UUID categoryPageId);
}
