package vn.tts.model.payload.user;

import jakarta.validation.constraints.NotBlank;
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
public class RolePayload implements Serializable {
    @NotBlank(message = "{validate.role.name.not.blank}")
    private String name;

    @NotBlank(message = "{validate.role.code.not.blank}")
    private String code;

    @NotBlank(message = "{validate.role.description.not.blank}")
    private String description;

    @NotEmpty(message = "{validate.role.permissionIds.not.blank}")
    private List<UUID> permissionIds;
}
