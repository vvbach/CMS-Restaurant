package vn.tts.model.response.image;

import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ImageWebResponse extends PublishableResponse {
    private UUID id;
    private String description;
    private String pathImage;
}
