package vn.tts.model.payload.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Getter
@Setter
@FieldNameConstants
public class LoginPayload implements Serializable {

    private String username;
    private String password;

    public @NotBlank(message = "{validate.login.payload.password.not.blank}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "{validate.login.payload.password.format}")
    @Length(max = 100, message = "{validate.login.payload.password.length}") String getPassword() {
        return password;
    }

    public @NotBlank(message = "{validate.login.payload.username.not.blank}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{validate.login.payload.username.format}")
    @Length(max = 100, message = "{validate.login.payload.username.length}") String getUsername() {
        return username;
    }
}
