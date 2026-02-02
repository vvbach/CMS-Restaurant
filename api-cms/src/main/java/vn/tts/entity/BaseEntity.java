package vn.tts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vn.tts.config.audit.AuditNameEntityListener;
import vn.tts.enums.DeleteEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Audited
@MappedSuperclass
@FieldNameConstants
@NoArgsConstructor
@SuperBuilder
@EntityListeners({AuditingEntityListener.class, AuditNameEntityListener.class})
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    protected UUID id;

    @Column(name = "is_delete", nullable = false, columnDefinition = "smallint")
    protected DeleteEnum isDelete;

    @NotAudited
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "created_by_id", columnDefinition = "uuid", updatable = false)
    @CreatedBy
    protected UUID createdBy;

    @NotAudited
    @Column(name = "created_by_name", updatable = false)
    protected String createdByName;

    @NotAudited
    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "updated_by_id", columnDefinition = "uuid")
    @LastModifiedBy
    protected UUID updatedBy;

    @Column(name = "updated_by_name")
    protected String updatedByName;

    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        isDelete = DeleteEnum.NO;
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
}