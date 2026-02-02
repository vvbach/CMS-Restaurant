package vn.tts.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.auth.PermissionEntity;
import vn.tts.entity.auth.RoleEntity;
import vn.tts.entity.auth.RolePermissionRelation;
import vn.tts.enums.DeleteEnum;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.dto.RoleDto;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.user.RolePayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.auth.PermissionResponse;
import vn.tts.model.response.auth.RoleDetailResponse;
import vn.tts.model.response.auth.RoleResponse;
import vn.tts.repository.auth.RolePermissionRepository;
import vn.tts.repository.auth.RoleRepository;
import vn.tts.service.BaseService;

import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService extends BaseService {
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ModelMapper modelMapper;
    private final PermissionService permissionService;
    
    public RoleResponse findById(UUID id) {
        RoleEntity roleEntity = roleRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("id", getMessage("message.entity.not.found")));
        return getResponse(roleEntity);
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(this::getResponse)
                .toList();
    }

    public RoleDetailResponse getRoleDetailById(UUID id) {
        List<RoleDto> dtos = roleRepository.getRoleDtoById(id);
        RoleDetailResponse response = modelMapper.map(dtos.getFirst(), RoleDetailResponse.class);
        response.setId(dtos.getFirst().getRoleId());
        response.setCode(dtos.getFirst().getRoleCode());
        response.setName(dtos.getFirst().getRoleName());

        response.setPermissions(new ArrayList<>());
        dtos.forEach(dto -> response.getPermissions().add(new PermissionResponse(
                dto.getPermissionId(),
                dto.getPermissionName(),
                dto.getPermissionCode()
        )));
        return response;
    }
    
    public PaginationResponse<List<RoleResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<RoleEntity> data = roleRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<RoleResponse>> response = new PaginationResponse<>();
        response.setData(
                data.getContent()
                        .stream()
                        .map(this::getResponse)
                        .toList()
        );
        response.setTotal(data.getTotalElements());
        return response;
    }
    
    @Transactional
    public RoleDetailResponse create(RolePayload payload) {
        RoleEntity roleEntity = modelMapper.map(payload, RoleEntity.class);
        RoleEntity createdEntity = roleRepository.save(roleEntity);
        UUID roleId = roleEntity.getId();

        Set<UUID> permissionIds = validateAndGetValidPermissionIds(payload.getPermissionIds());
        return saveAndGetResponse(createdEntity, roleId, permissionIds);
    }

    @Transactional
    public RoleDetailResponse update(UUID id, RolePayload payload) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("id", getMessage("message.entity.not.found")));
        if (DeleteEnum.YES.equals(entity.getIsDelete())) {
            throw new AppBadRequestException("id", getMessage("role.deleted"));
        }

        if (entity.isDefault())
            throw new AppBadRequestException("id", getMessage("message.update.default.role"));

        entity.setName(payload.getName());
        entity.setCode(payload.getCode());
        roleRepository.save(entity);

        Set<UUID> permissionIds = validateAndGetValidPermissionIds(payload.getPermissionIds());
        rolePermissionRepository.deleteByRoleId(id);
        return saveAndGetResponse(entity, id, permissionIds);
    }
    
    @Transactional
    public void delete(UUID id) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("id", getMessage("message.entity.not.found")));
        if (DeleteEnum.YES.equals(entity.getIsDelete())) {
            throw new AppBadRequestException("id", getMessage("role.deleted"));
        }

        if (entity.isDefault())
            throw new AppBadRequestException("id", getMessage("message.delete.default.role"));
        entity.setIsDelete(DeleteEnum.YES);
        roleRepository.save(entity);
    }

    private RoleResponse getResponse(RoleEntity entity) {
        return modelMapper.map(entity, RoleResponse.class);
    }

    private Set<UUID> validateAndGetValidPermissionIds(List<UUID> permissionIdList) {
        Set<UUID> permissionIds = new HashSet<>(permissionIdList);
        List<PermissionResponse> permissions = permissionService.findAllByIds(permissionIds);
        if (permissions.size() < permissionIds.size())
            throw new AppBadRequestException("permissionIds", getMessage("validate.permission.not.exists"));

        return permissionIds;
    }

    private RoleDetailResponse saveAndGetResponse(RoleEntity entity, UUID roleId, Set<UUID> permissionIds) {
        List<RolePermissionRelation> relations = permissionIds.stream()
                .map(permissionId -> new RolePermissionRelation(roleId, permissionId))
                .toList();
        rolePermissionRepository.saveAll(relations);

        RoleDetailResponse response = modelMapper.map(entity, RoleDetailResponse.class);
        List<PermissionResponse> permissions = permissionService.findAllByIds(permissionIds);
        response.setPermissions(permissions);
        return response;
    }
}
