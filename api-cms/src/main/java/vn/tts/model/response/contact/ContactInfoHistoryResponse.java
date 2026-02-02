package vn.tts.model.response.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoHistoryResponse extends PublishableHistoryResponse implements Serializable {
    private UUID id;
    private String text;
    private String imageUrl;
    private String address;
    private String email;
    private String phoneNumber;
}
