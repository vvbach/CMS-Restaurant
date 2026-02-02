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
@AuditTable("featured_category_aud")
@Table(name = "featured_category")
@SQLDelete(sql = "update featured_category set is_delete = 1 where id = ?")
public class FeaturedCategoryEntity extends PublishableEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_id")
    public UUID categoryId;

    @Column(nullable = false, name = "image_url")
    public String imageUrl;

    @Column(nullable = false, name = "description")
    public String description;
}
