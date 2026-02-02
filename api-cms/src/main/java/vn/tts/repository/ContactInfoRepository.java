package vn.tts.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.ContactInfoEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public interface ContactInfoRepository extends
        JpaRepository<ContactInfoEntity, UUID>,
        RevisionRepository<ContactInfoEntity, UUID, Integer> {

    @Query("""
        select c from ContactInfoEntity c where
        (:#{#payload.description} is null
            or c.text like concat(:#{#payload.description}, '%')
            or c.address like concat(:#{#payload.description}, '%')
            or c.email like concat(:#{#payload.description}, '%')
            or c.phoneNumber like concat(:#{#payload.description}, '%')
            or c.createdByName like concat(:#{#payload.description}, '%')
        )
        and (:#{#payload.isDelete} is null or c.isDelete = :#{#payload.isDelete})
        and (:#{#payload.status} is null or c.status = :#{#payload.status})
        and (CAST(:fromDate AS timestamp) is null or c.createdAt >= :fromDate)
        and (CAST(:toDate AS timestamp) is null or c.createdAt < :toDate)
    """)
    Page<ContactInfoEntity> filter(FilterPayload payload,
                                   @Param("fromDate") OffsetDateTime fromDate,
                                   @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}

