package vn.tts.model.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.entity.auth.PermissionEntity;
import vn.tts.enums.DeleteEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse implements Serializable {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private boolean isDefault;
    private DeleteEnum isDelete;
    private String createdByName;
    private Instant createdAt;
    private String updatedByName;
    private Instant updatedAt;
}
