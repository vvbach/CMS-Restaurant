package vn.tts.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import vn.tts.entity.auth.RoleUserRelation;
import vn.tts.model.response.auth.RoleUserResponse;
import vn.tts.model.response.user.UserDetailResponse;

import java.util.List;
import java.util.UUID;

public interface RoleUserRepository extends
        JpaRepository<RoleUserRelation, UUID>,
        RevisionRepository<RoleUserRelation, UUID, Integer> {

    @Query("""
            SELECT NEW vn.tts.model.response.auth.RoleUserResponse(
                r.id, r.name, r.code
            )
            FROM RoleEntity r
            JOIN RoleUserRelation ru ON ru.roleId = r.id
            WHERE ru.userId = :userId
            AND r.isDelete = 0
            AND ru.isDelete = 0
            """)
    List<RoleUserResponse> getRolesByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT NEW vn.tts.model.response.user.UserDetailResponse(
                u.id, u.username, u.fullName, u.phone, u.email, u.gender, u.status,
                u.createdByName, u.createdAt
            )
            FROM UserEntity u
            JOIN RoleUserRelation ru ON u.id = ru.userId
            WHERE ru.roleId = :roleId
            """)
    List<UserDetailResponse> getUsersByRoleId(@Param("roleId") UUID roleId);

    @Modifying
    @Query("""
    DELETE FROM RoleUserRelation ru
    WHERE ru.userId = :userId
    """)
    void deleteByUserId(@Param("userId") UUID userId);
}
