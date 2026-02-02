package vn.tts.model.payload.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserPayload implements Serializable {
    private String fullName;
    private String username;
    private String phone;
    private String email;
    private GenderEnum gender;
    private UserStatusEnum status;
    private String createdByName;
    private LocalDate fromDate;
    private LocalDate toDate;
}

