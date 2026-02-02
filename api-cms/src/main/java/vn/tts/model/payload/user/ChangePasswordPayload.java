package vn.tts.model.payload.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@FieldNameConstants
public class ChangePasswordPayload {
    private String oldPassword;
    private String newPassword;

    public @NotBlank(message = "{validate.login.payload.password.not.blank}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "{validate.login.payload.password.format}")
    @Length(max = 100, message = "{validate.login.payload.password.length}") String getOldPassword() {
        return oldPassword;
    }

    public @NotBlank(message = "{validate.login.payload.password.not.blank}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "{validate.login.payload.password.format}")
    @Length(max = 100, message = "{validate.login.payload.password.length}") String getNewPassword() {
        return newPassword;
    }
}
