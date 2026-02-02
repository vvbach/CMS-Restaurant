package vn.tts.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.auth.RolePermissionRelation;

import java.util.UUID;

public interface RolePermissionRepository extends
        JpaRepository<RolePermissionRelation, UUID>,
        RevisionRepository<RolePermissionRelation, UUID, Integer> {
    @Modifying
    @Query("""
        DELETE FROM RolePermissionRelation rp
        WHERE rp.roleId = :roleId
    """)
    void deleteByRoleId(@Param("roleId") UUID roleId);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}