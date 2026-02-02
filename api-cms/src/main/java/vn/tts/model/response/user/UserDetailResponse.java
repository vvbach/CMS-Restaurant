package vn.tts.model.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailResponse implements Serializable {
    private UUID userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private GenderEnum gender;
    private UserStatusEnum status;
    private String createdByName;
    private Instant createdAt;
}