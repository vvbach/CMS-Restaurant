package vn.tts.config.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.tts.model.UserPrincipal;
import vn.tts.entity.BaseEntity;

public class AuditNameEntityListener {
    @PrePersist
    public void touchCreatedByName(Object target) {
        if (!(target instanceof BaseEntity aud)) return;

        // only set if not already set (defensive)
        if (aud.getCreatedByName() != null) return;

        String name = getCurrentUsername();
        if (name != null) {
            aud.setCreatedByName(name);
            aud.setUpdatedByName(name);
        }
    }

    @PreUpdate
    public void touchUpdatedByName(Object target) {
        if (!(target instanceof BaseEntity aud)) return;
        String name = getCurrentUsername();
        if (name != null) {
            aud.setUpdatedByName(name);
        }
    }

    private String getCurrentUsername(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        try {
            UserPrincipal userInfoDetails = (UserPrincipal) auth.getPrincipal();
            return userInfoDetails.getFullName();
        } catch (IllegalArgumentException ex) {
            // if name is not a UUID, return the raw name
            return auth.getName();
        } catch (Exception e) {
            // repository not available or DB error — swallow and return null so not to break persistence
            return null;
        }
    }
}
