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
public class CategoryBestFoodPayload implements Serializable {
    @NotNull(message = "{validate.category.best.food.categoryPageId.not.null}")
    private UUID categoryPageId;

    @NotNull(message = "{validate.category.best.food.foodId.not.null}")
    private UUID foodId;

    @NotBlank(message = "{validate.category.best.food.description.not.blank}")
    private String description;
}
