package vn.tts.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.tts.entity.auth.PermissionEntity;
import vn.tts.model.response.auth.PermissionResponse;
import vn.tts.repository.auth.PermissionRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public List<PermissionResponse> findAll() {
        List<PermissionEntity> permissionEntityList = permissionRepository.findAll();
        return permissionEntityList.stream()
                .map(entity -> new PermissionResponse(
                        entity.getId(),
                        entity.getName(),
                        entity.getCode()
                ))
                .toList();
    }

    public List<PermissionResponse> findAllByIds(Collection<UUID> permissionIds) {
        List<PermissionEntity> permissionEntityList = permissionRepository.findAllById(permissionIds);
        return permissionEntityList.stream()
                .map(entity -> new PermissionResponse(
                        entity.getId(),
                        entity.getName(),
                        entity.getCode()
                ))
                .toList();
    }
}