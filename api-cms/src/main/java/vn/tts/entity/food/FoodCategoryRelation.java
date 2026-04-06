package vn.tts.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import vn.tts.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "food_category_relation")
@Getter
@Setter
@Audited
@AuditTable(value = "food_category_relation_aud")
@SQLRestriction(value = "is_delete = 0")
public class FoodCategoryRelation extends BaseEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_id")
    private UUID foodId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_category_id")
    private UUID foodCategoryId;
}
