package vn.tts.model.payload.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Getter
@Setter
public class DeletePayload implements Serializable {
    @NotBlank(message = "{validate.reason.delete.blank}")
    @Length(max = 500, message = "{validate.reason.delete.blank.length}")
    @Schema(description = "Ly do xóa không quá 500 ký tự", example = "Description test lí do xóa")
    private String reason;
}
