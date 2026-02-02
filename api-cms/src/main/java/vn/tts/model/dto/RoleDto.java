package vn.tts.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.DeleteEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto implements Serializable {
    private UUID roleId;
    private String roleName;
    private String roleCode;
    private UUID permissionId;
    private String permissionName;
    private String permissionCode;
    private boolean isDefault;
    private DeleteEnum isDelete;
    private String createdByName;
    private Instant createdAt;
    private String updatedByName;
    private Instant updatedAt;
}
