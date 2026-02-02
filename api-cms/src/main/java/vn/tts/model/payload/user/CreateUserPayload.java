package vn.tts.model.payload.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import vn.tts.enums.GenderEnum;

import java.io.Serializable;

@Getter
@Setter
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserPayload implements Serializable {
    @NotBlank(message = "{validate.login.payload.username.not.blank}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{validate.login.payload.username.format}")
    @Length(max = 100, message = "{validate.login.payload.username.length}")
    private String username;

    @NotBlank(message = "{validate.register.payload.fullname.not.blank}")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s\\\\|.]+$", message = "{validate.register.payload.fullname.format}")
    @Length(max = 100, message = "{validate.register.payload.fullname.length}")
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
    private GenderEnum gender;
}