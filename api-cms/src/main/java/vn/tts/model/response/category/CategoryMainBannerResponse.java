package vn.tts.model.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CategoryMainBannerResponse extends PublishableResponse {
    private UUID id;
    private UUID categoryPageId;
    private UUID foodId;
    private String title;
    private String description;
    private String imageUrl;
}
