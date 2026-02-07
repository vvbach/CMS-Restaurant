package vn.tts.model.response.food;

import lombok.*;
import vn.tts.model.response.FoodCategoryResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodResponse implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stockQuantity;
    private List<FoodCategoryResponse> categories;

    public FoodResponse(
            UUID id,
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            BigDecimal discount,
            Integer stockQuantity
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discount = discount;
        this.stockQuantity = stockQuantity;
    }
}
