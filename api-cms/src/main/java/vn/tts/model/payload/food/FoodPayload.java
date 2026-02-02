package vn.tts.model.payload.food;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FoodPayload implements Serializable {
    @NotBlank(message = "{validate.food.payload.name.not.blank}")
    private String name;

    @NotBlank(message = "{validate.food.payload.description.not.blank}")
    private String description;

    @NotBlank(message = "{validate.food.payload.image.url.not.blank}")
    private String imageUrl;

    @NotNull(message = "{validate.food.payload.price.not.null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validate.food.payload.price.min}")
    private BigDecimal price;

    @Min(value = 0, message = "{validate.food.payload.discount.min}")
    @Max(value = 100, message = "{validate.food.payload.discount.max}")
    private BigDecimal discount;

    @NotNull(message = "{validate.food.payload.stock.quantity.not.null}")
    @Min(value = 0, message = "{validate.food.payload.stock.quantity.min}")
    private Integer stockQuantity;

    @NotNull(message = "{validate.food.payload.category.id.not.null}")
    private List<UUID> categoryIds;
}
