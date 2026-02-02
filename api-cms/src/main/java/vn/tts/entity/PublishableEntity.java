package vn.tts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import vn.tts.enums.ContentStatus;

import java.io.Serializable;

@Setter
@Getter
@MappedSuperclass
@FieldNameConstants
@NoArgsConstructor
@SuperBuilder
@Audited
@AuditOverride(forClass = BaseEntity.class)
public abstract class PublishableEntity extends BaseEntity implements Serializable {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ContentStatus status;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "unpublish_reason", length = 500)
    private String unpublishReason;

    @Override
    protected void onCreate() {
        super.onCreate();
        status = ContentStatus.DRAFT;
    }
}
