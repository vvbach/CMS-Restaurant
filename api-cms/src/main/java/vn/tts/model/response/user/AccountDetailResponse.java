package vn.tts.model.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;
import vn.tts.model.response.auth.RoleResponse;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailResponse {
    private String username;
    private String fullName;
    private String avatar;
    private String phone;
    private String email;
    private List<RoleResponse> roles;
    private GenderEnum gender;
    private UserStatusEnum status;
}
