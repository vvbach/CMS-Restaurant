package vn.tts.model.payload.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeBestFoodPayload implements Serializable {
    @NotNull(message = "{validate.home.best.food.foodId.not.null}")
    private UUID foodId;

    @NotBlank(message = "{validate.home.best.food.description.not.blank}")
    private String description;
}
