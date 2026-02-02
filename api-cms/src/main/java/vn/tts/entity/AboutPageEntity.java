package vn.tts.entity;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "about_page")
@Audited
@AuditTable("about_page_aud")
@SQLDelete(sql = "update about_page set is_delete = 1 where id = ?")
public class AboutPageEntity extends PublishableEntity {
    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;
}
