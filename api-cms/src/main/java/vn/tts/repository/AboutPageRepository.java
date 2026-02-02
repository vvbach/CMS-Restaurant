package vn.tts.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.AboutPageEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AboutPageRepository extends JpaRepository<AboutPageEntity, UUID>, RevisionRepository<AboutPageEntity, UUID, Integer> {
    @Query("""
        select a from AboutPageEntity a where
        (:#{#payload.description} is null
            or a.title like concat(:#{#payload.description}, '%')
            or a.createdByName like concat(:#{#payload.description}, '%')
        )
        and (:#{#payload.isDelete} is null or a.isDelete = :#{#payload.isDelete})
        and (:#{#payload.status} is null or a.status = :#{#payload.status})
        and (CAST(:fromDate AS timestamp) is null or a.createdAt >= :fromDate)
        and (CAST(:toDate AS timestamp) is null or a.createdAt < :toDate)
    """)
    Page<AboutPageEntity> filter(FilterPayload payload,
                                 @Param("fromDate") OffsetDateTime fromDate,
                                 @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}