package vn.tts.entity.food;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "food_category_relation")
@EntityListeners(AuditingEntityListener.class)
public class FoodCategoryRelation implements Serializable {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    protected UUID id;

    @Column(name = "publication_date", updatable = false)
    @CreatedDate
    private Instant publicationDate;

    @PrePersist
    protected void onCreate() {
        this.publicationDate = Instant.now();
    }

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_id")
    private UUID foodId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, name = "food_category_id")
    private UUID foodCategoryId;
}
