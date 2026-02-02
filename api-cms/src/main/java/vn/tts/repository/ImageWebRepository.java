package vn.tts.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.ImageWebEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface ImageWebRepository
        extends JpaRepository<ImageWebEntity, UUID>,
        RevisionRepository<ImageWebEntity, UUID, Integer> {

    @Query("""
        select img from ImageWebEntity img where
        (:#{#payload.description} is null or img.description like concat(:#{#payload.description}, '%'))
        and ( :#{#payload.createdByName} is null or img.createdByName like concat(:#{#payload.createdByName}, '%')  )
        and ( :#{#payload.isDelete} is null or img.isDelete = :#{#payload.isDelete} )
        and ( :#{#payload.status} is null or img.status = :#{#payload.status} )
        and ( CAST(:fromDate AS timestamp) is null or ( img.createdAt >= :fromDate))
        and ( CAST(:toDate AS timestamp) is null  or (img.createdAt < :toDate) )
    """)
    Page<ImageWebEntity> filter(FilterPayload payload,
                                @Param("fromDate") OffsetDateTime fromDate,
                                @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
