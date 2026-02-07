package vn.tts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@FieldNameConstants
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    protected UUID id;

    @Column(name = "publication_date", updatable = false)
    @CreatedDate
    private Instant publicationDate;

    @PrePersist
    protected void onCreate() {
        this.publicationDate = Instant.now();
    }
}
