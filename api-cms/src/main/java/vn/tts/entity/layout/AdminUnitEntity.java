package vn.tts.entity.layout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import vn.tts.entity.PublishableEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@AuditTable("admin_unit_aud")
@Table(name = "admin_unit")
@SQLDelete(sql = "update admin_unit set is_delete = 1 where id = ?")
public class AdminUnitEntity extends PublishableEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "logo_url")
    private String logoUrl;
}
