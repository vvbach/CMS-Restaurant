package vn.tts.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tts.config.security.JwtService;
import vn.tts.entity.UserEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.exception.LoginFailedException;
import vn.tts.exception.UserNotFoundException;
import vn.tts.model.payload.auth.RefreshTokenPayload;
import vn.tts.model.payload.auth.RegisterPayload;
import vn.tts.model.payload.user.ChangePasswordPayload;
import vn.tts.model.payload.auth.LoginPayload;
import vn.tts.model.response.auth.ForceChangePasswordResponse;
import vn.tts.model.response.auth.LoginResponse;
import vn.tts.model.response.auth.RefreshTokenResponse;
import vn.tts.repository.UserRepository;
import vn.tts.service.BaseService;
import vn.tts.service.user.AccountService;

import java.util.UUID;

import static vn.tts.service.user.ValidationUtils.checkUserDelete;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService extends BaseService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginPayload payload) {
        UserEntity account = userRepository.findByUsername(payload.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(account);

        if (!passwordEncoder.matches(payload.getPassword(), account.getPassword())) {
            throw new LoginFailedException("message.login.fail");
        }

        String accountId = account.getId().toString();

        if (account.getForceChangePassword() == 1)
            return LoginResponse.builder()
                    .changePasswordToken(jwtService.generateChangePasswordToken(accountId))
                    .changePasswordRequired(true)
                    .build();

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(accountId))
                .refreshToken(jwtService.generateRefreshToken(accountId))
                .changePasswordRequired(false)
                .build();
    }

    @Transactional
    public String register(RegisterPayload payload) {
        accountService.existsByEmail(payload.getEmail());
        accountService.existsByPhone(payload.getPhone());
        accountService.existsByUsername(payload.getUsername());
        UserEntity entity = modelMapper.map(payload, UserEntity.class);
        entity.setPassword(passwordEncoder.encode(payload.getPassword()));
        entity.setForceChangePassword(0);
        userRepository.save(entity);

        return getMessage("message.register.success");
    }

    public String logout() {
        jwtService.deleteSession(getUserDetail().getUserId());
        return getMessage("message.logout.success");
    }

    public RefreshTokenResponse refreshToken(RefreshTokenPayload payload) {
        String refreshToken = payload.getRefreshToken();
        if (!jwtService.validateRefreshToken(refreshToken))
            throw new AppBadRequestException(RefreshTokenPayload.Fields.refreshToken, getMessage("jwt.invalid"));

        UserEntity user = userRepository.findById(UUID.fromString(jwtService.extractAccountId(refreshToken)))
                .orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(user);

        return RefreshTokenResponse.builder()
                .accessToken(
                        jwtService.generateToken(
                                jwtService.extractAccountId(refreshToken)
                        ))
                .build();
    }

    public ForceChangePasswordResponse forceChangePassword(String authHeader, ChangePasswordPayload payload) {
        String token = jwtService.extractTokenFromHeader(authHeader);
        if (!jwtService.validateChangePasswordToken(token)) {
            throw new AppBadRequestException("token", getMessage("jwt.invalid"));
        }

        UUID userId = UUID.fromString(jwtService.extractAccountId(token));
        UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(user);

        if (payload.getOldPassword().equals(payload.getNewPassword()))
            throw new AppBadRequestException(ChangePasswordPayload.Fields.newPassword, getMessage("change.password.identical"));

        if (!passwordEncoder.matches(payload.getOldPassword(), user.getPassword()))
            throw new LoginFailedException("change.password.wrong.password");

        user.setPassword(passwordEncoder.encode(payload.getNewPassword()));
        user.setForceChangePassword(0);
        userRepository.save(user);
        System.out.println("User " + user.getUsername() + " has changed their password.");
        return ForceChangePasswordResponse.builder()
                .accessToken(jwtService.generateToken(String.valueOf(userId)))
                .refreshToken(jwtService.generateRefreshToken(String.valueOf(userId)))
                .build();
    }
}
