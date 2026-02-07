package vn.tts.entity.layout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import vn.tts.entity.BaseEntity;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@Table(name = "social_link")
public class SocialLinkEntity extends BaseEntity {
    @Column(nullable = false, name = "url")
    private String url;

    @Column(nullable = false, name = "platform")
    private String platform;

    @Column(nullable = false, name = "icon_url")
    private String iconUrl;
}
