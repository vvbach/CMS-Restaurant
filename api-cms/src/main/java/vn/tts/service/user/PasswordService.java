package vn.tts.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.tts.config.security.JwtService;
import vn.tts.entity.UserEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.exception.LoginFailedException;
import vn.tts.exception.UserNotFoundException;
import vn.tts.model.payload.user.ChangePasswordPayload;
import vn.tts.repository.UserRepository;
import vn.tts.service.BaseService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static vn.tts.service.user.ValidationUtils.checkUserDelete;


@Service
@RequiredArgsConstructor
public class PasswordService extends BaseService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public String changePassword(ChangePasswordPayload payload) {
        UUID userId = getUserDetail().getUserId();
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(userEntity);

        if (payload.getOldPassword().equals(payload.getNewPassword()))
            throw new AppBadRequestException(ChangePasswordPayload.Fields.newPassword, "change.password.identical");

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (!encoder.matches(payload.getOldPassword(), userEntity.getPassword()))
            throw new LoginFailedException("change.password.wrong.password");

        userEntity.setPassword(encoder.encode(payload.getNewPassword()));
        userRepository.save(userEntity);

        return getMessage("change.password.success");
    }

    @Transactional
    public String resetPassword(UUID id) {
        jwtService.deleteSession(id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(userEntity);

        String newPassword = generatePassword();
        userEntity.setPassword(PasswordEncoderFactories
                .createDelegatingPasswordEncoder()
                .encode(newPassword));
        userEntity.setForceChangePassword(1);
        userRepository.save(userEntity);
        return newPassword;
    }

    public String generatePassword() {
        String combinedChars = getCombinedChars();
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    @NotNull
    private String getCombinedChars() {
        RandomStringUtils rng = RandomStringUtils.secure();

        String upperCaseLetters = rng.next(2, 65, 90, true, true);
        String lowerCaseLetters = rng.next(2, 97, 122, true, true);
        String numbers = rng.nextNumeric(2);
        String specialChar = rng.next(2, "@$!%*?&");
        String totalChars = rng.nextAlphanumeric(2);
        return upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar)
                .concat(totalChars);
    }
}
