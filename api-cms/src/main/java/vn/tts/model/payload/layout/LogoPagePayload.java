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
public class LogoPagePayload implements Serializable {
    @NotBlank(message = "{validate.logo.page.payload.name.not.blank}")
    private String name;

    @NotBlank(message = "{validate.logo.page.payload.url.not.blank}")
    private String url;
}
