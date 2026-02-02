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
@AuditTable("logo_page_aud")
@Table(name = "logo_page")
@SQLDelete(sql = "update logo_page set is_delete = 1 where id = ?")
public class LogoPageEntity extends PublishableEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "url")
    private String url;
}
