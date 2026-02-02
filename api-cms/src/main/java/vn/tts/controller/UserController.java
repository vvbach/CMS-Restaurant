package vn.tts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.tts.model.payload.user.*;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.auth.RoleUserResponse;
import vn.tts.model.response.user.AccountDetailResponse;
import vn.tts.model.response.user.UserAuditResponse;
import vn.tts.model.response.user.UserDetailResponse;
import vn.tts.model.response.user.UserHistoryResponse;
import vn.tts.service.auth.RoleUserService;
import vn.tts.service.user.AccountService;
import vn.tts.service.user.PasswordService;
import vn.tts.service.user.UserQueryService;

import java.util.List;
import java.util.UUID;
@Validated
@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User Controller")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final RoleUserService roleUserService;
    private final AccountService accountService;
    private final UserQueryService userQueryService;
    private final PasswordService passwordService;

    // Current user API

    @Operation(description = "Get current user's detailed information")
    @GetMapping("/current")
    public ResponseEntity<ResponseBase<AccountDetailResponse>> getCurrentAccount() throws Exception {
        return ResponseBase.success(accountService.getCurrentAccount());
    }

    @Operation(description = "Update information of current user")
    @PutMapping(value = "/current",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBase<String>> updateCurrentUser(
            @Parameter(description = "Image file uploaded to server",
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestPart("payload") UserPayload payload
    ){
        return ResponseBase.success(accountService.updateCurrentAccount(file, payload));
    }

    @Operation(description = "Change password of currently logged-in user")
    @PostMapping("/current/change-password")
    public ResponseEntity<ResponseBase<String>> changePassword(@RequestBody @Valid ChangePasswordPayload payload) {
        return ResponseBase.success(passwordService.changePassword(payload));
    }

    // User API

    @Operation(description = "Get detailed information of a specific user")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponseBase<UserAuditResponse>> getUserDetail(@PathVariable UUID id) throws Exception {
        return ResponseBase.success(userQueryService.getUserAudit(id));
    }

    @Operation(description = "Create a user account by admin")
    @PostMapping("/")
    @PreAuthorize("hasAuthority('USER_ADD')")
    public ResponseEntity<ResponseBase<String>> create(@RequestBody @Valid CreateUserPayload payload) {
        return ResponseBase.success(accountService.create(payload));
    }

    @Operation(description = "Update user information by admin")
    @PutMapping(value = "/{id}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ResponseBase<String>> update(
            @PathVariable("id") UUID id,
            @Parameter(description = "Image file uploaded to server",
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestPart("payload") UserPayload payload
    ) throws Exception {
        return ResponseBase.success(accountService.update(id, file, payload));
    }

    @Operation(description = "Delete a user account")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ResponseBase<String>> delete(@PathVariable UUID id) {
        return ResponseBase.success(accountService.delete(id));
    }

    @Operation(description = "Restore a previously deleted account")
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_RESTORE')")
    public ResponseEntity<ResponseBase<String>> restore(@PathVariable UUID id) {
        return ResponseBase.success(accountService.restore(id));
    }

    @Operation(description = "Find all users with search query")
    @GetMapping("")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<UserDetailResponse>>>> listUsers(
            @ModelAttribute SearchUserPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(userQueryService.listUser(payload, page, pageSize));
    }

    @Operation(description = "List user edit, delete, and create history")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponseBase<List<UserHistoryResponse>>> getUserHistory(
            @PathVariable UUID id
    ) {
        return ResponseBase.success(userQueryService.getUserHistory(id));
    }

    // Role API
    @Operation(description = "Update user's role")
    @PutMapping(value = "/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ResponseBase<List<RoleUserResponse>>> updateUserRoles(
            @PathVariable("id") UUID id, @Valid @RequestBody RoleUserPayload payload) {
        return ResponseBase.success(roleUserService.update(id, payload));
    }

    @Operation(description = "Get user roles")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ResponseBase<List<RoleUserResponse>>> getUserRoles(@PathVariable("id") UUID id) {
        return ResponseBase.success(roleUserService.getRolesByUserId(id));
    }


    @Operation(description = "Reset a user’s password and return the new password")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_RESET_PASSWORD')")
    public ResponseEntity<ResponseBase<String>> resetPassword(@PathVariable UUID id) {
        return ResponseBase.success(passwordService.resetPassword(id));
    }

    @Operation(description = "Disable a user account")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('USER_UPDATE_STATUS')")
    public ResponseEntity<ResponseBase<String>> disable(@PathVariable UUID id) {
        return ResponseBase.success(accountService.disable(id));
    }

    @Operation(description = "Enable a user account")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('USER_UPDATE_STATUS')")
    public ResponseEntity<ResponseBase<String>> enable(@PathVariable UUID id) {
        return ResponseBase.success(accountService.enable(id));
    }
    

    @Operation(summary = "Check if email exists",
            description = "This API sends a request to check whether an email address has already been registered.")
    @GetMapping("/check/email")
    public ResponseEntity<ResponseBase<Void>> existsByEmail(@RequestParam(value = "email") String email) {
        accountService.existsByEmail(email);
        return ResponseBase.success(null);
    }

    @Operation(description = "Check if a phone number has already been registered")
    @GetMapping("/check/phone")
    public ResponseEntity<ResponseBase<Void>> existsByPhone(@RequestParam(value = "phone") String phone) {
        accountService.existsByPhone(phone);
        return ResponseBase.success(null);
    }

    @Operation(description = "Check if a username has already been registered")
    @GetMapping("/check/username")
    public ResponseEntity<ResponseBase<Void>> existsByUsername(@RequestParam(value = "username") String username) {
        accountService.existsByUsername(username);
        return ResponseBase.success(null);
    }
}
