package vn.tts.model.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class RefreshTokenResponse implements Serializable {
    private String accessToken;
}
