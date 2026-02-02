package vn.tts.model.response.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContactInfoResponse extends PublishableResponse {
    private UUID id;
    private String text;
    private String imageUrl;
    private String address;
    private String email;
    private String phoneNumber;
}
