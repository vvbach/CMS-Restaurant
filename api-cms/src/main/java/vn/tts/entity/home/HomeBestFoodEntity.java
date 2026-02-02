package vn.tts.entity.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import vn.tts.entity.PublishableEntity;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@AuditTable(value = "home_best_food_aud")
@Table(name = "home_best_food")
@SQLDelete(sql = "update home_best_food set is_delete = 1 where id = ?")
public class HomeBestFoodEntity extends PublishableEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_id")
    private UUID foodId;

    @Column(nullable = false, name = "description")
    private String description;
}
