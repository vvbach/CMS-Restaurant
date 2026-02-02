package vn.tts.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.tts.entity.SystemConfigEntity;
import vn.tts.entity.UserEntity;
import vn.tts.entity.auth.PermissionEntity;
import vn.tts.entity.auth.RoleEntity;
import vn.tts.entity.auth.RolePermissionRelation;
import vn.tts.entity.auth.RoleUserRelation;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;
import vn.tts.repository.SystemConfigRepository;
import vn.tts.repository.UserRepository;
import vn.tts.repository.auth.PermissionRepository;
import vn.tts.repository.auth.RolePermissionRepository;
import vn.tts.repository.auth.RoleRepository;
import vn.tts.repository.auth.RoleUserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private static final String SECURITY_INIT_KEY = "SECURITY_INIT_DONE";

    private final SystemConfigRepository systemConfigRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleUserRepository roleUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (systemConfigRepository.existsById(SECURITY_INIT_KEY)) return;

        initPermissions();
        initRoles();
        initAdminUser();

        systemConfigRepository.save(new SystemConfigEntity(SECURITY_INIT_KEY, "true"));
    }

    private void initPermissions() {
        List<PermissionEntity> permissions = List.of(
                new PermissionEntity("UI management", "UI"),
                new PermissionEntity("Read UI", "UI_READ"),
                new PermissionEntity("Add UI", "UI_ADD"),
                new PermissionEntity("Update UI", "UI_UPDATE"),
                new PermissionEntity("Delete UI", "UI_DELETE"),
                new PermissionEntity("UI pending approval", "UI_PENDING_APPROVE"),
                new PermissionEntity("Approve UI", "UI_APPROVE"),
                new PermissionEntity("Reject UI", "UI_REJECT"),
                new PermissionEntity("Publish UI", "UI_PUBLISH"),
                new PermissionEntity("Unpublish UI", "UI_UNPUBLISH"),
                new PermissionEntity("Draft UI", "UI_DRAFT"),

                new PermissionEntity("Product management", "PRODUCT"),
                new PermissionEntity("Read product", "PRODUCT_READ"),
                new PermissionEntity("Add product", "PRODUCT_ADD"),
                new PermissionEntity("Update product", "PRODUCT_UPDATE"),
                new PermissionEntity("Delete product", "PRODUCT_DELETE"),
                new PermissionEntity("Product pending approval", "PRODUCT_PENDING_APPROVE"),
                new PermissionEntity("Approve product", "PRODUCT_APPROVE"),
                new PermissionEntity("Reject product", "PRODUCT_REJECT"),
                new PermissionEntity("Publish product", "PRODUCT_PUBLISH"),
                new PermissionEntity("Unpublish product", "PRODUCT_UNPUBLISH"),
                new PermissionEntity("Draft product", "PRODUCT_DRAFT"),

                new PermissionEntity("Role management", "ROLE"),
                new PermissionEntity("Read role", "ROLE_READ"),
                new PermissionEntity("Add role", "ROLE_ADD"),
                new PermissionEntity("Update role", "ROLE_UPDATE"),
                new PermissionEntity("Delete role", "ROLE_DELETE"),

                new PermissionEntity("User management", "USER"),
                new PermissionEntity("Read user", "USER_READ"),
                new PermissionEntity("Add user", "USER_ADD"),
                new PermissionEntity("Update user", "USER_UPDATE"),
                new PermissionEntity("Delete user", "USER_DELETE"),
                new PermissionEntity("Restore user", "USER_RESTORE"),
                new PermissionEntity("Reset user password", "USER_RESET_PASSWORD"),
                new PermissionEntity("Update user status", "USER_UPDATE_STATUS")
        );

        permissionRepository.saveAll(permissions);
    }

    private void initRoles() {
        RoleEntity admin = createRole("Administrator", "ADMIN");
        RoleEntity uiEditor = createRole("UI Editor", "UI_EDITOR");
        RoleEntity uiDeputy = createRole("UI Deputy Editor in Chief", "UI_DEPUTY_EDITOR_IN_CHIEF");
        RoleEntity uiChief = createRole("UI Editor in Chief", "UI_EDITOR_IN_CHIEF");

        RoleEntity productEditor = createRole("Product Editor", "PRODUCT_EDITOR");
        RoleEntity productDeputy = createRole("Product Deputy Editor in Chief", "PRODUCT_DEPUTY_EDITOR_IN_CHIEF");
        RoleEntity productChief = createRole("Product Editor in Chief", "PRODUCT_EDITOR_IN_CHIEF");

        permissionRepository.findAll()
                .forEach(p -> bindPermission(admin, p.getCode()));

        bindPermissions(uiEditor, List.of("UI_READ", "UI_ADD", "UI_UPDATE", "UI_DRAFT"));
        bindPermissions(uiDeputy, List.of("UI_PENDING_APPROVE", "UI_APPROVE", "UI_REJECT"));
        bindPermissions(uiChief, List.of("UI_PUBLISH", "UI_UNPUBLISH"));

        bindPermissions(productEditor, List.of("PRODUCT_READ", "PRODUCT_ADD", "PRODUCT_UPDATE", "PRODUCT_DRAFT"));
        bindPermissions(productDeputy, List.of("PRODUCT_PENDING_APPROVE", "PRODUCT_APPROVE", "PRODUCT_REJECT"));
        bindPermissions(productChief, List.of("PRODUCT_PUBLISH", "PRODUCT_UNPUBLISH"));
    }

    private RoleEntity createRole(String name, String code) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> {
                    RoleEntity r = new RoleEntity();
                    r.setName(name);
                    r.setCode(code);
                    return roleRepository.save(r);
                });
    }

    private void bindPermissions(RoleEntity role, List<String> permissionCodes) {
        permissionCodes.forEach(code -> bindPermission(role, code));
    }

    private void bindPermission(RoleEntity role, String permissionCode) {
        PermissionEntity permission = permissionRepository.findByCode(permissionCode).orElseThrow();
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            rolePermissionRepository.save(
                    new RolePermissionRelation(role.getId(), permission.getId())
            );
        }
    }

    private void initAdminUser() {
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@2025"));
        admin.setFullName("Administrator");
        admin.setEmail("admin@gmail.com");
        admin.setPhone("0000000000");
        admin.setGender(GenderEnum.MALE);
        admin.setStatus(UserStatusEnum.ACTIVE);

        userRepository.save(admin);

        RoleEntity adminRole = roleRepository.findByCode("ADMIN").orElseThrow();
        roleUserRepository.save(new RoleUserRelation(adminRole.getId(), admin.getId()));

        System.out.println("Default admin created: admin / Admin@2025");
    }
}

