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
public class CategoryMainBannerUpdatePayload implements Serializable {
    @NotNull(message = "{validate.category.main.banner.payload.foodId.not.blank}")
    private UUID foodId;

    @NotBlank(message = "{validate.category.main.banner.payload.title.not.blank}")
    private String title;

    @NotBlank(message = "{validate.category.main.banner.payload.description.not.blank}")
    private String description;

    @NotBlank(message = "{validate.category.main.banner.payload.imageUrl.not.blank}")
    private String imageUrl;
}
