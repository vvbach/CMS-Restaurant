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
@Table(name = "logo_page")
public class LogoPageEntity extends BaseEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "url")
    private String url;
}
