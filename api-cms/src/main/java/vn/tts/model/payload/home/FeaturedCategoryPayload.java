package vn.tts.model.payload.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FeaturedCategoryPayload {
    @NotNull(message = "{validate.featured.category.categoryId.not.null}")
    private UUID categoryId;

    @NotBlank(message = "{validate.featured.category.imageUrl.not.blank}")
    private String imageUrl;

    @NotBlank(message = "{validate.featured.category.description.not.blank}")
    private String description;
}
