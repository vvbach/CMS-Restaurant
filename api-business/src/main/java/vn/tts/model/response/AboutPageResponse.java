package vn.tts.model.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AboutPageResponse implements Serializable {
    private String title;
    private String text;
    private String imageUrl;
}
