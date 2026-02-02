package vn.tts.model.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AboutPagePayload implements Serializable {
    @NotBlank(message = "{validate.about.banner.payload.title.not.blank}")
    private String title;

    @NotBlank(message = "{validate.about.banner.payload.text.not.blank}")
    private String text;

    @NotBlank(message = "{validate.about.banner.payload.image.url.not.blank}")
    private String imageUrl;
}
