package vn.tts.model.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class LoginResponse implements Serializable {
    private String accessToken;
    private String refreshToken;
    private String changePasswordToken;
    private boolean changePasswordRequired;
}
