package vn.tts.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tts.entity.UserEntity;
import vn.tts.model.dto.AccountDto;
import vn.tts.model.dto.UserPrincipalDto;
import vn.tts.model.payload.user.SearchUserPayload;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface UserRepository extends
        JpaRepository<UserEntity, UUID>,
        RevisionRepository<UserEntity, UUID, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    @Query(value = """
            
              SELECT EXISTS (
                SELECT 1
                FROM UserEntity u
                WHERE u.email = :email
            )
            """)
    Boolean existsByEmail(@Param("email") String email);

    @Query(value = """
            
              SELECT EXISTS (
                SELECT 1
                FROM UserEntity u
                WHERE u.phone = ?#{#phone}
            )
            """)
    Boolean existsByPhone(String phone);

    @Query(value = """
            
              SELECT EXISTS (
                SELECT 1
                FROM UserEntity u
                WHERE u.username = ?#{#username}
            )
            """)
    Boolean existsByUsername(String username);

    @Query(value = """
            SELECT NEW vn.tts.model.dto.AccountDto(
                u.username, u.fullName, u.avatar, u.phone, u.email, u.gender, u.status,
                r.id, r.name, r.code
            )
            FROM UserEntity u
            LEFT JOIN RoleUserRelation ru ON u.id = ru.userId
            LEFT JOIN RoleEntity r ON r.id = ru.roleId
            WHERE u.id = :userId
            """)
    List<AccountDto> getAccountDetail(@Param("userId") UUID userId);

    @Query("""
                SELECT u FROM UserEntity u WHERE
                (:#{#payload.createdByName} IS NULL OR u.createdByName LIKE CONCAT(:#{#payload.createdByName}, '%'))
                AND ( :#{#payload.email} IS NULL OR u.email LIKE CONCAT(:#{#payload.email}, '%'))
                AND ( :#{#payload.fullName} IS NULL OR u.fullName LIKE CONCAT(:#{#payload.fullName}, '%'))
                AND ( :#{#payload.phone} IS NULL OR u.phone LIKE CONCAT(:#{#payload.phone}, '%'))
                AND ( :#{#payload.username} IS NULL OR u.username LIKE CONCAT(:#{#payload.username}, '%'))
                AND ( :#{#payload.gender} IS NULL OR u.gender = :#{#payload.gender})
                AND ( :#{#payload.status} IS NULL OR u.status = :#{#payload.status})
                AND ( CAST(:fromDate AS TIMESTAMP) IS NULL OR (u.createdAt >= :fromDate))
                AND ( CAST(:toDate AS TIMESTAMP) IS NULL OR (u.createdAt <= :toDate))
            """)
    Page<UserEntity> filter(
            SearchUserPayload payload,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query(value = """
            SELECT
              u.id,
              u.username,
              u.fullname,
              u.password,
              r.code AS role,
              STRING_AGG(p.code, ',') AS permissions
            FROM users u
            LEFT JOIN role_user ru
              ON u.id = ru.user_id
            LEFT JOIN role r
              ON ru.role_id = r.id
            LEFT JOIN role_permission rp
              ON rp.role_id = r.id
            LEFT JOIN permission p
              ON rp.permission_id = p.id
            WHERE u.id = :userId
            GROUP BY u.id, u.username, u.fullname, u.password, r.code
            """, nativeQuery = true)
    Optional<UserPrincipalDto> findUserInfoDetailById(@Param("userId") UUID userId);

    @Query(value = """
            SELECT
              u.id,
              u.username,
              u.fullname,
              u.password,
              r.code AS role,
              STRING_AGG(p.code, ',') AS permissions
            FROM users u
            LEFT JOIN role_user ru
              ON u.id = ru.user_id
            LEFT JOIN role r
              ON ru.role_id = r.id
            LEFT JOIN role_permission rp
              ON rp.role_id = r.id
            LEFT JOIN permission p
              ON rp.permission_id = p.id
            WHERE u.username = :username
            GROUP BY u.id, u.username, u.fullname, u.password, r.code
            """, nativeQuery = true)
    Optional<UserPrincipalDto> findUserInfoDetailByUsername(@Param("username") String username);

    @Modifying
    @Query(value = "UPDATE public.users SET is_delete = 0 WHERE id = :userId", nativeQuery = true)
    int restoreById(@Param("userId") UUID userId);
}
