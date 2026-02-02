package vn.tts.repository.home;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.home.FeaturedCategoryEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface FeaturedCategoryRepository extends
        JpaRepository<FeaturedCategoryEntity, UUID>,
        RevisionRepository<FeaturedCategoryEntity, UUID, Integer> {
    @Query("""
        SELECT e FROM FeaturedCategoryEntity e WHERE
        (:#{#payload.description} IS NULL OR e.description LIKE CONCAT('%', :#{#payload.description}, '%'))
        AND (:#{#payload.createdByName} IS NULL OR e.createdByName LIKE CONCAT('%', :#{#payload.createdByName}, '%'))
        AND (:#{#payload.isDelete} IS NULL OR e.isDelete = :#{#payload.isDelete})
        AND (:#{#payload.status} IS NULL OR e.status = :#{#payload.status})
        AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR e.createdAt >= :fromDate)
        AND (CAST(:toDate AS TIMESTAMP) IS NULL OR e.createdAt < :toDate)
    """)
    Page<FeaturedCategoryEntity> filter(FilterPayload payload,
                                        @Param("fromDate") OffsetDateTime fromDate,
                                        @Param("toDate") OffsetDateTime toDate,
                                        Pageable pageable);
}
