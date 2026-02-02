package vn.tts.model.payload.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleUserPayload implements Serializable {
    @NotEmpty(message = "{validate.role.user.roleIds.not.empty}")
    private List<UUID> roleIds;
}

