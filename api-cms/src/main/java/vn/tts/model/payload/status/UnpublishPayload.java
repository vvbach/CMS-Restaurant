package vn.tts.model.payload.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UnpublishPayload {
    @NotBlank(message = "{validate.article.reason.unpublish.blank}")
    @Length(max = 500, message = "{validate.article.reason.unpublish.blank.length}")
    @Schema(description = "Mô tả image với 500 ký tự ", example = "Mô tả test lý do từ chối")
    private String reason;
}
