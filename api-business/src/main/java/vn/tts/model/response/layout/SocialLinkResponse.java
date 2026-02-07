package vn.tts.model.response.layout;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialLinkResponse implements Serializable {
    private String url;
    private String platform;
    private String iconUrl;
}
