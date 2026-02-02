package vn.tts.model.payload.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import vn.tts.enums.GenderEnum;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPayload implements Serializable {

    @NotBlank(message = "{validate.register.payload.full.name.not.blank}")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\\\|.]+$", message = "{validate.register.payload.fullname.format}")
    @Length(max = 100, message = "{validate.register.payload.full.name.length}")
    private String fullName;

    @NotBlank(message = "{validate.register.payload.phone.not.blank}")
    @Length(max = 20, message = "{validate.register.payload.phone.length}")
    @Pattern(regexp = "^[0-9]+$", message = "{validate.register.payload.phone.format}")
    private String phone;

    @Length(max = 100, message = "{validate.register.payload.email.length}")
    @Email(message = "{validate.register.payload.email.format}")
    @NotBlank(message = "{validate.register.payload.email.not.blank}")
    private String email;

    @NotNull(message = "{validate.register.payload.gender.not.null}")
    public GenderEnum gender;
}
