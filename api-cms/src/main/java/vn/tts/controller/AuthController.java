package vn.tts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.payload.auth.LoginPayload;
import vn.tts.model.payload.auth.RefreshTokenPayload;
import vn.tts.model.payload.auth.RegisterPayload;
import vn.tts.model.payload.user.ChangePasswordPayload;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.auth.ForceChangePasswordResponse;
import vn.tts.model.response.auth.LoginResponse;
import vn.tts.model.response.auth.RefreshTokenResponse;
import vn.tts.service.auth.AuthService;

@Validated
@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and Authorization Controller")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Login",
            description = "Normally this will log in and return an access token and a refresh token. "
                    + "If the account is required to change its password, it will only return a change-password token."
    )
    @PostMapping("/login")
    public ResponseEntity<ResponseBase<LoginResponse>> login(@RequestBody @Valid LoginPayload payload) {
        return ResponseBase.success(authService.login(payload));
    }

    @Operation(description = "Register a new account")
    @PostMapping("/register")
    public ResponseEntity<ResponseBase<String>> register(@RequestBody @Valid RegisterPayload payload) {

        return ResponseBase.success(authService.register(payload));
    }

    @Operation(description = "Use a refresh token to obtain a new access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseBase<RefreshTokenResponse>> refresh(@RequestBody @Valid RefreshTokenPayload payload) {
        return ResponseBase.success(authService.refreshToken(payload));
    }

    @Operation(
            description = "Users change their password after first login or after their password has been reset. "
                    + "Before changing the password, the user must log in to receive a change-password token."
    )
    @PostMapping("/force-change-password")
    public ResponseEntity<ResponseBase<ForceChangePasswordResponse>> forceChangePassword(
            HttpServletRequest request,
            @RequestBody @Valid ChangePasswordPayload payload
    ) {
        return ResponseBase.success(authService.forceChangePassword(request.getHeader("Authorization"), payload));
    }

    @Operation(description = "Logout current account")
    @PostMapping("/logout")
    public ResponseEntity<ResponseBase<String>> logout() {
        return ResponseBase.success(authService.logout());
    }
}
