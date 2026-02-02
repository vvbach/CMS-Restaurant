package vn.tts.model.payload.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

@Getter
@Setter
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenPayload implements Serializable {
    @NotBlank(message = "validate.refresh.payload.not.blank")
    private String refreshToken;
}

