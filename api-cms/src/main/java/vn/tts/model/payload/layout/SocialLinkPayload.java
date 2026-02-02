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
public class SocialLinkPayload implements Serializable {
    @NotBlank(message = "{validate.social.link.payload.url.not.blank}")
    private String url;

    @NotBlank(message = "{validate.social.link.payload.platform.not.blank}")
    private String platform;

    @NotBlank(message = "{validate.social.link.payload.icon.url.not.blank}")
    private String iconUrl;
}
