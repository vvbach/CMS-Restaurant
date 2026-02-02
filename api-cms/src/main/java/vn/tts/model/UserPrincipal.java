package vn.tts.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal {
    private UUID userId;
    private String username;
    private String fullName;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;
}

