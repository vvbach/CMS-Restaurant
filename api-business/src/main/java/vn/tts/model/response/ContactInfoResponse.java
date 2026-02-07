package vn.tts.model.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfoResponse implements Serializable {
    private String text;
    private String imageUrl;
    private String address;
    private String email;
    private String phoneNumber;
}
