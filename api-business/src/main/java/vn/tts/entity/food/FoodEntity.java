package vn.tts.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import vn.tts.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "food")
public class FoodEntity extends BaseEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "description")
    private String description;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2, name = "price")
    private BigDecimal price;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(nullable = false, name = "stock_quantity")
    private Integer stockQuantity;
}
