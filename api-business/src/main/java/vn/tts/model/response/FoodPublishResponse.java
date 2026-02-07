package vn.tts.model.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodPublishResponse implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stockQuantity;
    private List<FoodCategoryResponse> categories;
}
