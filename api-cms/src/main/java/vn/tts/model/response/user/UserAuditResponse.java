package vn.tts.model.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.DeleteEnum;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAuditResponse implements Serializable {
    private UUID id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private GenderEnum gender;
    private UserStatusEnum status;
    private Instant createdAt;
    private String createdByName;
    private Instant updatedAt;
    private String updatedByName;
    private DeleteEnum isDelete;
    private String avatar;
    private List<UserHistoryResponse> history;
}
