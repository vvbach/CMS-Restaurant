package vn.tts.model.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfoPayload {
    @NotBlank(message = "{validate.contact.info.payload.text.not.blank}")
    private String text;

    @NotBlank(message = "{validate.contact.info.payload.image.url.not.blank}")
    private String imageUrl;

    @NotBlank(message = "{validate.contact.info.payload.address.not.blank}")
    private String address;

    @NotBlank(message = "{validate.contact.info.payload.email.not.blank}")
    @Pattern(
            regexp = "^\\s*[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\s*(,\\s*[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\s*)*$",
            message = "{validate.contact.info.payload.email.format}"
    )
    private String email;

    @NotBlank(message = "{validate.contact.info.payload.phone.number.not.blank}")
    @Pattern(
            regexp = "^\\s*(\\+?\\d[\\d\\s().-]{6,}\\d)\\s*(,\\s*(\\+?\\d[\\d\\s().-]{6,}\\d)\\s*)*$",
            message = "{validate.contact.info.payload.phone.number.format}"
    )
    private String phoneNumber;

}
