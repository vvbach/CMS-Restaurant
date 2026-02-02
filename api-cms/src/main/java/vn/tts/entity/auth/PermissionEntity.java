package vn.tts.entity.auth;

import jakarta.persistence.*;
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
@Table(name = "permission")
@Audited
@AuditTable(value = "permission_aud")
public class PermissionEntity extends BaseEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(unique = true, nullable = false, name = "code")
    private String code;
}

