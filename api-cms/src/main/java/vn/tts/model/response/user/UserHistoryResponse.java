package vn.tts.model.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.history.RevisionMetadata;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserHistoryResponse implements Serializable {
    private Instant eventDate;
    private RevisionMetadata.RevisionType revisionType;
    private UUID userId;
    private String username;
    private String fullName;
    private String avatar;
    private String email;
    private String phone;
    private GenderEnum gender;
    private UserStatusEnum status;
}
