package vn.tts.repository.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.auth.RoleEntity;
import vn.tts.model.dto.RoleDto;
import vn.tts.model.payload.FilterPayload;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends
        JpaRepository<RoleEntity, UUID>,
        RevisionRepository<RoleEntity, UUID, Integer> {

    Optional<RoleEntity> findByCode(String code);

    @Query("""
            SELECT NEW vn.tts.model.dto.RoleDto(
                r.id, r.name, r.code, p.id, p.name, p.code,
                r.isDefault, r.isDelete, r.createdByName, r.createdAt, r.updatedByName, r.updatedAt
            )
            FROM RoleEntity r
            JOIN RolePermissionRelation rp ON r.id = rp.roleId
            JOIN PermissionEntity p ON rp.permissionId = p.id
            WHERE rp.isDelete = 0
            AND r.id = :id
            """)
    List<RoleDto> getRoleDtoById(@Param("id") UUID id);

    @Query("""
            SELECT r
            FROM RoleEntity r
            WHERE r.isDelete = 0
            AND r.id IN :roleIds
            """)
    List<RoleEntity> findNonDeletedByIds(@Param("roleIds") Collection<UUID> roleIds);

    @Query("""
            SELECT e FROM RoleEntity e WHERE
            (:#{#payload.description} IS NULL OR e.name LIKE CONCAT('%', :#{#payload.description}, '%'))
            AND (:#{#payload.createdByName} IS NULL OR e.createdByName LIKE CONCAT('%', :#{#payload.createdByName}, '%'))
            AND (:#{#payload.isDelete} IS NULL OR e.isDelete = :#{#payload.isDelete})
            AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR e.createdAt >= :fromDate)
            AND (CAST(:toDate AS TIMESTAMP) IS NULL OR e.createdAt < :toDate)
            """)
    Page<RoleEntity> filter(FilterPayload payload,
                            @Param("fromDate") OffsetDateTime fromDate,
                            @Param("toDate") OffsetDateTime toDate,
                            Pageable pageable);

}
