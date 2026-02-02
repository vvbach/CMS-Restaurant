package vn.tts.service.user;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.tts.config.security.JwtService;
import vn.tts.entity.UserEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.enums.UserStatusEnum;
import vn.tts.exception.AppBadRequestException;
import vn.tts.exception.UserNotFoundException;
import vn.tts.model.dto.AccountDto;
import vn.tts.model.payload.user.CreateUserPayload;
import vn.tts.model.payload.user.UserPayload;
import vn.tts.model.response.user.AccountDetailResponse;
import vn.tts.repository.UserRepository;
import vn.tts.service.BaseService;
import vn.tts.service.auth.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static vn.tts.service.user.ValidationUtils.checkUserDelete;

@Service
@RequiredArgsConstructor
public class AccountService extends BaseService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    private final static String PATH_DEFAULT = "image/user/";

    @Transactional
    public String create(CreateUserPayload payload) {
        if (userRepository.existsByEmail(payload.getEmail())) {
            throw new AppBadRequestException(CreateUserPayload.Fields.email, getMessage("check.exist.mail.using"));
        }
        if (Objects.isNull(payload.getPhone())
                || payload.getPhone().isEmpty()
                || userRepository.existsByPhone(payload.getPhone())) {
            throw new AppBadRequestException(CreateUserPayload.Fields.phone, getMessage("check.exist.phone.using"));
        }
        if (Objects.isNull(payload.getUsername())
                || payload.getUsername().isEmpty()
                || userRepository.existsByUsername(payload.getUsername())) {
            throw new AppBadRequestException(CreateUserPayload.Fields.username, getMessage("check.exist.username.using"));
        }

        UserEntity entity = modelMapper.map(payload, UserEntity.class);

        String password = passwordService.generatePassword();
        entity.setPassword(passwordEncoder.encode(password));

        entity.setForceChangePassword(1);

        userRepository.save(entity);
        return password;
    }

    @Transactional
    public String update(UUID userId, MultipartFile file, UserPayload payload) throws Exception {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));
        checkUserDelete(userEntity);

        updateUser(userEntity, file, payload);

        return getMessage("base.message.update.obj", "user");
    }


    @Transactional
    public String delete(UUID id) {
        UserEntity usersEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(usersEntity);

        usersEntity.setStatus(UserStatusEnum.INACTIVE);
        usersEntity.setIsDelete(DeleteEnum.YES);
        userRepository.save(usersEntity);

        jwtService.deleteSession(usersEntity.getId());

        return getMessage("base.message.delete.obj", "user");
    }

    @Transactional
    public String updateCurrentAccount(MultipartFile file, UserPayload payload) {
        UserEntity usersEntity = userRepository.findById(getUserDetail().getUserId()).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));
        checkUserDelete(usersEntity);
        updateUser(usersEntity, file, payload);

        return getMessage("base.message.update.obj", "account");
    }


    public AccountDetailResponse getCurrentAccount() throws Exception {
        UUID userId = getUserDetail().getUserId();
        List<AccountDto> dtos = userRepository.getAccountDetail(userId);
        AccountDetailResponse response = modelMapper.map(dtos.getFirst(), AccountDetailResponse.class);
        response.setRoles(new ArrayList<>());
        dtos.forEach(dto -> response.getRoles().add(roleService.getRoleDetailById(dto.getRoleId())));

        if (response.getAvatar() != null && !response.getAvatar().isEmpty())
            response.setAvatar(minioService.getPreSignedUrl(response.getAvatar()));

        return response;
    }

    @Transactional
    public String restore(UUID id) {
        int updated = userRepository.restoreById(id);

        if (updated == 0) {
            throw new UserNotFoundException("message.user.not.found");
        }

        UserEntity usersEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));
        usersEntity.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(usersEntity);

        return getMessage("message.restore.user");
    }

    @Transactional
    public String disable(UUID id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(user);

        user.setStatus(UserStatusEnum.INACTIVE);
        userRepository.save(user);
        return getMessage("base.message.status.obj", "user");
    }
    
    @Transactional
    public String enable(UUID id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        checkUserDelete(user);

        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);
        return getMessage("base.message.status.obj", "user");
    }



    private void updateUser(UserEntity usersEntity, MultipartFile file, UserPayload payload) {
        String avatarUrl = null;

        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            avatarUrl = uploadImageMinio(file, PATH_DEFAULT);
        }

        usersEntity.setEmail(payload.getEmail());
        usersEntity.setPhone(payload.getPhone());
        usersEntity.setFullName(payload.getFullName());
        usersEntity.setGender(payload.getGender());
        usersEntity.setAvatar(avatarUrl);
        userRepository.save(usersEntity);
    }

    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppBadRequestException(email, getMessage("check.exist.mail.using"));
        }
    }

    public void existsByPhone(String phone) {
        if (Objects.isNull(phone) || phone.isEmpty()) return;
        if (userRepository.existsByPhone(phone)) {
            throw new AppBadRequestException(phone, getMessage("check.exist.phone.using"));
        }
    }

    public void existsByUsername(String username) {
        if (Objects.isNull(username) || username.isEmpty()) return;
        if (userRepository.existsByUsername(username)) {
            throw new AppBadRequestException(username, getMessage("check.exist.username.using"));
        }
    }
}
