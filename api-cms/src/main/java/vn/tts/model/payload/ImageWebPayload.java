package vn.tts.model.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageWebPayload implements Serializable {

    @NotBlank(message = "{validate.image.description.blank}")
    @Length(max = 500, message = "{validate.image.description.length}")
    @Schema(description = "Mô tả image với 500 ký tự ", example = "Mô tả test")
    private String description;

}
