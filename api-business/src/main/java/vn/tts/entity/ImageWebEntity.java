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
@Table(name = "image_web")
public class ImageWebEntity extends BaseEntity {
    /**path này được lưu trên minio*/
    @Column(nullable = false, name = "path-image", length = 1000)
    private String pathImage;

    /**mô tả ảnh này*/
    @Column(nullable = true, name = "description", length = 500)
    private String description;
}
