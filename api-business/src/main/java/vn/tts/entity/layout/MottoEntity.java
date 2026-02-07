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
@Table(name = "motto")
public class MottoEntity extends BaseEntity {
    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "description")
    private String description;
}
