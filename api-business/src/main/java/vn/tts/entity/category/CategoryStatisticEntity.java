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
@Table(name = "category_statistic")
public class CategoryStatisticEntity extends BaseEntity {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "category_page_id")
    private UUID categoryPageId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "category_id")
    private UUID categoryId;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "description")
    private String description;

    @Column(nullable = false, name = "imageUrl")
    private String imageUrl;
}
