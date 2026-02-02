package vn.tts.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.auth.PermissionResponse;
import vn.tts.service.auth.PermissionService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Controller cho quyền người dùng")
public class PermissionController {
    private final PermissionService permissionService;

    @Operation(description = "Tìm tất cả các quyền")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ResponseBase<List<PermissionResponse>>> findAll() {
        return ResponseBase.success(permissionService.findAll());
    }
}
