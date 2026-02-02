package vn.tts.model.payload.category;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AboutCategoryPayload implements Serializable {
    @NotNull(message = "{validate.about.category.categoryPageId.not.null}")
    private UUID categoryPageId;

    @NotBlank(message = "{validate.about.category.title.not.blank}")
    private String title;

    @NotBlank(message = "{validate.about.category.subtitle.not.blank}")
    private String subtitle;

    @NotBlank(message = "{validate.about.category.description.not.blank}")
    private String description;

}
