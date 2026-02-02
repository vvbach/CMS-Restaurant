package vn.tts.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.GenderEnum;
import vn.tts.enums.UserStatusEnum;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {
    private String username;
    private String fullName;
    private String avatar;
    private String phone;
    private String email;
    private GenderEnum gender;
    private UserStatusEnum status;
    private UUID roleId;
    private String roleName;
    private String roleCode;
}
