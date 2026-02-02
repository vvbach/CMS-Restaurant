package vn.tts.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import vn.tts.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
@Audited
@AuditTable(value = "role_aud")
public class RoleEntity extends BaseEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(unique = true, nullable = false, name = "code")
    private String code;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Override
    protected void onCreate() {
        super.onCreate();
        isDefault = false;
    }
}
