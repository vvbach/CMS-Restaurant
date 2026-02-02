package vn.tts.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import vn.tts.entity.auth.RoleEntity;
import vn.tts.entity.auth.RoleUserRelation;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.payload.user.RoleUserPayload;
import vn.tts.model.response.auth.RoleUserResponse;
import vn.tts.model.response.user.UserDetailResponse;
import vn.tts.repository.auth.RoleRepository;
import vn.tts.repository.auth.RoleUserRepository;
import vn.tts.service.BaseService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleUserService extends BaseService {
    private final RoleUserRepository roleUserRepository;
    private final RoleRepository roleRepository;

    public List<RoleUserResponse> getRolesByUserId(UUID userId) {
        return roleUserRepository.getRolesByUserId(userId);
    }

    public List<UserDetailResponse> getUsersByRoleId(UUID roleId) {
        return roleUserRepository.getUsersByRoleId(roleId);
    }

    @Transactional
    public List<RoleUserResponse> update(UUID userId, RoleUserPayload payload) {
        Set<UUID> roleIds = new HashSet<>(payload.getRoleIds());
        List<RoleEntity> roles = roleRepository.findNonDeletedByIds(roleIds);
        if (roles.size() < roleIds.size())
            throw new AppBadRequestException("roleIds", getMessage("validate.role.not.exists"));

        roleUserRepository.deleteByUserId(userId);

        List<RoleUserRelation> relations = roleIds.stream()
                .map(roleId -> new RoleUserRelation(roleId, userId))
                .toList();
        roleUserRepository.saveAll(relations);

        return roles.stream()
                .map(role -> new RoleUserResponse(role.getId(), role.getName(), role.getCode()))
                .toList();
    }
}
