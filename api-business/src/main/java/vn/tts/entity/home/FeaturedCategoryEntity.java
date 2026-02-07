package vn.tts.entity.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.tts.entity.BaseEntity;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "featured_category")
public class FeaturedCategoryEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "category_id")
    public UUID categoryId;

    @Column(nullable = false, name = "image_url")
    public String imageUrl;

    @Column(nullable = false, name = "description")
    public String description;
}
