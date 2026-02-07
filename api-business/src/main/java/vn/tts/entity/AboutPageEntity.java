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
@Table(name = "about_page")
public class AboutPageEntity extends BaseEntity {
    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;
}
