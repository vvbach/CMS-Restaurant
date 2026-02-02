package vn.tts.model.dto;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPrincipalDto implements Serializable {
    private UUID userId;
    private String username;
    private String fullName;
    private String password;
    private String role;
    private String permissions;
}