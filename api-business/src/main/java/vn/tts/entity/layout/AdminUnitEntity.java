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
@Table(name = "admin_unit")
public class AdminUnitEntity extends BaseEntity {
    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "logo_url")
    private String logoUrl;
}
