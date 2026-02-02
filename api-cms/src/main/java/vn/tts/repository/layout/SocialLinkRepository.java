package vn.tts.repository.layout;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.layout.SocialLinkEntity;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional
public interface SocialLinkRepository extends
        JpaRepository<SocialLinkEntity, UUID>,
        RevisionRepository<SocialLinkEntity, UUID, Integer> {
    @Query("""
                select s from SocialLinkEntity s where
                (:#{#payload.description} is null
                    or s.url like concat('%', :#{#payload.description}, '%')
                    or s.platform like concat(:#{#payload.description}, '%')
                )
                and (:#{#payload.isDelete} is null or s.isDelete = :#{#payload.isDelete})
                and (:#{#payload.status} is null or s.status = :#{#payload.status})
                and (CAST(:fromDate AS timestamp) is null or s.createdAt >= :fromDate)
                and (CAST(:toDate AS timestamp) is null or s.createdAt < :toDate)
            """)
    Page<SocialLinkEntity> filter(FilterPayload payload,
                                  @Param("fromDate") OffsetDateTime fromDate,
                                  @Param("toDate") OffsetDateTime toDate, Pageable pageable);
}
