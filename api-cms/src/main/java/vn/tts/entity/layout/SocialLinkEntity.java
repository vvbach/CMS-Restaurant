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
@AuditTable("social_link_aud")
@Table(name = "social_link")
@SQLDelete(sql = "update social_link set is_delete = 1 where id = ?")
public class SocialLinkEntity extends PublishableEntity {
    @Column(nullable = false, name = "url")
    private String url;

    @Column(nullable = false, name = "platform")
    private String platform;

    @Column(nullable = false, name = "icon_url")
    private String iconUrl;
}
