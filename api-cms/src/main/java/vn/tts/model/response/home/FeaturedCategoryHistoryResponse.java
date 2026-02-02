package vn.tts.model.response.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedCategoryHistoryResponse extends PublishableHistoryResponse {
    private UUID id;
    private UUID categoryId;
    private String description;
    private String imageUrl;
}
