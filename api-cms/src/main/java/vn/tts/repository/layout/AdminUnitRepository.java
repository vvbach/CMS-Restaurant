package vn.tts.repository.layout;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.layout.AdminUnitEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public interface AdminUnitRepository extends
        JpaRepository<AdminUnitEntity, UUID>,
        RevisionRepository<AdminUnitEntity, UUID, Integer> {
    @Query("""
                select a from AdminUnitEntity a where
                (:#{#payload.description} is null
                    or a.name like concat(:#{#payload.description}, '%')
                )
                and (:#{#payload.isDelete} is null or a.isDelete = :#{#payload.isDelete})
                and (:#{#payload.status} is null or a.status = :#{#payload.status})
                and (CAST(:fromDate AS timestamp) is null or a.createdAt >= :fromDate)
                and (CAST(:toDate AS timestamp) is null or a.createdAt < :toDate)
            """)
    Page<AdminUnitEntity> filter(FilterPayload payload,
                                 @Param("fromDate") OffsetDateTime fromDate,
                                 @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
