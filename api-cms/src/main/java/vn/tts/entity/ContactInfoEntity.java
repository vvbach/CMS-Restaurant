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

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@AuditTable("contact_info_aud")
@Table(name = "contact_info")
@SQLDelete(sql = "update contact_info set is_delete = 1 where id = ?")
public class ContactInfoEntity extends PublishableEntity implements Serializable {
    @Column(nullable = false, name = "text")
    private String text;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    @Column(nullable = false, name = "address")
    private String address;

    @Column(nullable = false, name = "email")
    private String email;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;
}
