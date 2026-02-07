package vn.tts.entity.category;

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
@Table(name = "about_category")
public class AboutCategoryEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "category_page_id")
    private UUID categoryPageId;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "subtitle")
    private String subtitle;

    @Column(nullable = false, name = "description")
    private String description;
}
