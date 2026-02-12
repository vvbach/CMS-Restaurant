package vn.tts.model.payload.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class RejectPayload {
    @NotBlank(message = "{validate.reason.reject.blank}")
    @Length(max = 500, message = "{validate.reason.reject.blank.length}")
    @Schema(description = "Lý do từ chối với 500 ký tự ", example = "Description test lý do từ chối")
    private String reason;
}
