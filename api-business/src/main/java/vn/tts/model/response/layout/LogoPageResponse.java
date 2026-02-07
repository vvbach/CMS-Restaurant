package vn.tts.model.response.layout;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogoPageResponse implements Serializable {
    private String name;
    private String url;
}
