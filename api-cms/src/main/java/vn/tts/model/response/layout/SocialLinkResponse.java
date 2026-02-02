package vn.tts.model.response.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SocialLinkResponse extends PublishableResponse {
    private UUID id;
    private String url;
    private String platform;
    private String iconUrl;
}
