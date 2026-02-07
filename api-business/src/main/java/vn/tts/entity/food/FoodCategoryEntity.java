package vn.tts.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import vn.tts.entity.BaseEntity;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "food_categories")
public class FoodCategoryEntity extends BaseEntity {
    @Column(nullable = false, length = 50, name = "name", unique = true)
    private String name;

    @Column(name = "description")
    private String description;
}
