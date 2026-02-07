package vn.tts.entity.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.tts.entity.BaseEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "home_best_food")
public class HomeBestFoodEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_id")
    private UUID foodId;

    @Column(nullable = false, name = "description")
    private String description;
}
