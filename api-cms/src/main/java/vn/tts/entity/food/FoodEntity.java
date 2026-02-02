package vn.tts.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import vn.tts.entity.PublishableEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food")
@Audited
@AuditTable(value = "food_aud")
@SQLDelete(sql = "update food set is_delete = 1 where id = ?")
public class FoodEntity extends PublishableEntity {
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

