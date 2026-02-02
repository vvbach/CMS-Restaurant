package vn.tts.config.audit;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.tts.model.UserPrincipal;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    @Bean
    public AuditorAware<UUID> auditorProvider(){
        return new SecurityAuditorAware();
    }

    public static class SecurityAuditorAware implements AuditorAware<UUID> {
        @NonNull
        @Override
        public Optional<UUID> getCurrentAuditor() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null
                    || !auth.isAuthenticated()
                    || auth instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }

            UserPrincipal userDetails = (UserPrincipal) auth.getPrincipal();

            UUID id = userDetails.getUserId();
            try {
                return Optional.of(id);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
    }
}
