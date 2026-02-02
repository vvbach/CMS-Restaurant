package vn.tts.repository.layout;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.layout.LogoPageEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public interface LogoPageRepository extends
        JpaRepository<LogoPageEntity, UUID>,
        RevisionRepository<LogoPageEntity, UUID, Integer> {
    @Query("""
                select l from LogoPageEntity l where
                (:#{#payload.description} is null
                    or l.name like concat(:#{#payload.description}, '%')
                )
                and (:#{#payload.isDelete} is null or l.isDelete = :#{#payload.isDelete})
                and (:#{#payload.status} is null or l.status = :#{#payload.status})
                and (CAST(:fromDate AS timestamp) is null or l.createdAt >= :fromDate)
                and (CAST(:toDate AS timestamp) is null or l.createdAt < :toDate)
            """)
    Page<LogoPageEntity> filter(FilterPayload payload,
                                @Param("fromDate") OffsetDateTime fromDate,
                                @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
