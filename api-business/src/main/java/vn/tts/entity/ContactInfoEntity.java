package vn.tts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "contact_info")
public class ContactInfoEntity extends BaseEntity {
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
