package vn.tts.model.response.food;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.tts.enums.ContentStatus;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.response.PublishableResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class FoodResponse extends PublishableResponse {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private List<FoodCategoryResponse> categories;

    public FoodResponse(UUID id, String name, String description, String imageUrl,
                        BigDecimal price, BigDecimal discount, Integer stockQuantity,
                        List<FoodCategoryResponse> categories,
                        ContentStatus status, DeleteEnum isDelete,
                        String createdByName, Instant createdAt,
                        String updatedByName, Instant updatedAt,
                        String reasonDelete, String reasonRejection, String reasonUnpublish) {

        super(status, isDelete, createdByName, createdAt, updatedByName, updatedAt,
                reasonDelete, reasonRejection, reasonUnpublish);
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discount = discount;
        this.stockQuantity = stockQuantity;
        this.categories = categories;

        if (price != null && discount != null) {
            this.discountPrice = price.subtract(
                    price.multiply(discount)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            );
        }
    }

}
