package vn.tts.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import vn.tts.entity.UserEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.UserDetail;
import vn.tts.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class BaseService {
    @Autowired
    protected MinioService minioService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UserRepository userRepository;

    public String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException ex) {
            log.error(ex.getMessage(), code);
        }
        return StringUtils.EMPTY;
    }

    public UserDetail getUserDetail() {
        DecodedJWT decodedJWT = JWT.decode(getToken());
        UUID userId = UUID.fromString(decodedJWT.getSubject());
        UserEntity usersEntity = userRepository.findById(userId).orElse(null);
        assert usersEntity != null;

        return UserDetail.builder()
                .userId(userId)
                .userName(usersEntity.getUsername())
                .fullName(usersEntity.getFullName())
                .email(usersEntity.getEmail())
                .build();
    }

    public UserDetail getUserDetailById(UUID userId) {
        UserEntity usersEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppBadRequestException("status", getMessage("message.entity.not.found")));

        return UserDetail.builder()
                .userId(userId)
                .userName(usersEntity.getUsername())
                .fullName(usersEntity.getFullName())
                .email(usersEntity.getEmail())
                .build();
    }

    private String getToken() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getHeader("Authorization").substring("Bearer ".length());
    }

    public String uploadImageMinio(MultipartFile file, String pathDefault) {
        if (pathDefault == null) {
            pathDefault = StringUtils.EMPTY;
        }
        String pathUrl = pathDefault.concat(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            return minioService.uploadFile(file, pathUrl);
        } catch (Exception ex) {
            throw new AppBadRequestException("file", getMessage("message.upload.file.error"));
        }
    }

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png"
    );

    public void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Accept only extension JPG, PNG");
        }
    }

}
