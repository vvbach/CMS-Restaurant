package vn.tts.model.response.food;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodHistoryResponse extends PublishableHistoryResponse implements Serializable {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stockQuantity;
    private List<FoodCategoryResponse> categories;
}
