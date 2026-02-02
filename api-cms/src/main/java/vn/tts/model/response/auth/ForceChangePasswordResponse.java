package vn.tts.model.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForceChangePasswordResponse {
    private String accessToken;
    private String refreshToken;
}
