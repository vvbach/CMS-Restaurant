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
public class CategoryStatisticPayload implements Serializable {
    @NotNull(message = "{validate.category.statistic.categoryPageId.not.null}")
    private UUID categoryPageId;

    @NotNull(message = "{validate.category.statistic.categoryId.not.null}")
    private UUID categoryId;

    @NotBlank(message = "{validate.category.statistic.name.not.blank}")
    private String name;

    @NotBlank(message = "{validate.category.statistic.description.not.blank}")
    private String description;

    @NotBlank(message = "{validate.category.statistic.imageUrl.not.blank}")
    private String imageUrl;
}
