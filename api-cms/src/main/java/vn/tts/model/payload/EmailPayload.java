package vn.tts.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailPayload {
    @NotEmpty(message = "validate.email.payload.to.address.not.empty")
    private List<String> to;

    @NotBlank(message = "validate.email.payload.subject.not.blank")
    private String subject;

    @NotBlank(message = "validate.email.payload.message.not.blank")
    private String message;
}
