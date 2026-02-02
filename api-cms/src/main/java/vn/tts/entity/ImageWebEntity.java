package vn.tts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "image")
@Audited
@AuditTable("image_aud")
@SQLDelete(sql = "update image set is_delete = 1 where id = ?")
public class ImageWebEntity extends PublishableEntity {
    @Column(nullable = true, name = "description", length = 500)
    private String description;

    @Column(nullable = false, name = "path_image", length = 1000)
    private String pathImage;
}
