package vn.tts.model.payload.layout;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUnitPayload implements Serializable {
    @NotBlank(message = "{validate.admin.unit.payload.name.not.blank}")
    private String name;

    @NotBlank(message = "{validate.admin.unit.payload.logo.url.not.blank}")
    private String logoUrl;
}
