package vn.tts.model.payload.layout;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MottoPayload implements Serializable {
    @NotBlank(message = "{validate.motto.payload.title.not.blank}")
    private String title;

    @NotBlank(message = "{validate.motto.payload.description.not.blank}")
    private String description;
}
