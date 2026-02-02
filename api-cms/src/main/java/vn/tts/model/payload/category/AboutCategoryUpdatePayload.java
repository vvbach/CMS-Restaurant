package vn.tts.model.payload.category;

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
public class AboutCategoryUpdatePayload implements Serializable {
    @NotBlank(message = "{validate.about.category.title.not.blank}")
    private String title;

    @NotBlank(message = "{validate.about.category.subtitle.not.blank}")
    private String subtitle;

    @NotBlank(message = "{validate.about.category.description.not.blank}")
    private String description;
}
