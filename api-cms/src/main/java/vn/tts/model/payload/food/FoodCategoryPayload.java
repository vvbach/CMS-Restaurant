package vn.tts.model.payload.food;

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
public class FoodCategoryPayload implements Serializable {
    @NotBlank(message = "{validate.food.category.payload.name.not.blank}")
    private String name;

    private String description;
}
