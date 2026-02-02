package vn.tts.model.response.about;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AboutPageResponse extends PublishableResponse {
    private UUID id;
    private String title;
    private String text;
    private String imageUrl;
}
