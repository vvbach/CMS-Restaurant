package vn.tts.repository.layout;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tts.entity.layout.SocialLinkEntity;

import java.util.List;
import java.util.UUID;

public interface SocialLinkRepository extends JpaRepository<SocialLinkEntity, UUID> {
    @NotNull
    @Query("""
        SELECT e
        FROM SocialLinkEntity e
    """)
    List<SocialLinkEntity> getSocialLinks();
}
