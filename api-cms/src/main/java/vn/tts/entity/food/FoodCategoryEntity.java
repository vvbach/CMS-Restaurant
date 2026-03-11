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

@Entity
@Table(name = "food_category")
@Getter
@Setter
@Audited
@AuditTable(value = "food_category_aud")
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "update food_category set is_delete = 1 where id = ?")
public class FoodCategoryEntity extends PublishableEntity {
    @Column(nullable = false, length = 50, name = "name", unique = true)
    private String name;

    @Column(name = "description")
    private String description;
}
