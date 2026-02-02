package vn.tts.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.user.RolePayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.auth.RoleDetailResponse;
import vn.tts.model.response.auth.RoleResponse;
import vn.tts.model.response.user.UserDetailResponse;
import vn.tts.service.auth.RoleService;
import vn.tts.service.auth.RoleUserService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "Controller cho vai trò người dùng")
public class RoleController {
    private final RoleService roleService;
    private final RoleUserService roleUserService;

    @Operation(description = "Tìm tất cả các vai trò")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ResponseBase<List<RoleResponse>>> findAll() {
        return ResponseBase.success(roleService.findAll());
    }

    @Operation(description = "Tìm vai trò theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ResponseBase<RoleResponse>> findById(@PathVariable("id") UUID id) {
        return ResponseBase.success(roleService.findById(id));
    }

    @Operation(description = "Lấy chi tiết vai trò kèm theo các quyền theo ID")
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ResponseBase<RoleDetailResponse>> getRoleDetailById(@PathVariable("id") UUID id) {
        return ResponseBase.success(roleService.getRoleDetailById(id));
    }

    @Operation(description = "Filter vai trò")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<RoleResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(roleService.filter(payload, page, pageSize));
    }

    @Operation(description = "Tìm người dùng theo ID vai trò")
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('ROLE_READ') and hasAuthority('USER_READ')")
    public ResponseEntity<ResponseBase<List<UserDetailResponse>>> getUsersByRoleId(@PathVariable("id") UUID id) {
        return ResponseBase.success(roleUserService.getUsersByRoleId(id));
    }

    @Operation(description = "Tạo vai trò")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADD')")
    public ResponseEntity<ResponseBase<RoleDetailResponse>> create(@RequestBody RolePayload payload) {
        return ResponseBase.success(roleService.create(payload));
    }

    @Operation(description = "Cập nhật vai trò")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ResponseBase<RoleDetailResponse>> update(
            @PathVariable("id") UUID id, @Valid @RequestBody RolePayload payload
    ) {
        return ResponseBase.success(roleService.update(id, payload));
    }

    @Operation(description = "Xóa vai trò")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ResponseBase<String>> delete(@PathVariable("id") UUID id) {
        roleService.delete(id);
        return ResponseBase.success(null);
    }
}
