package vn.tts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetail {
    private UUID userId;
    private String userName;
    private String fullName;
    private String email;
}
