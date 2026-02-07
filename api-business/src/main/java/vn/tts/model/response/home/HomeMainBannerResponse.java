package vn.tts.model.response.home;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeMainBannerResponse implements Serializable {
    private UUID id;
    private UUID foodId;
    private String title;
    private String description;
    private String imageUrl;
}
