package vn.tts.model.response.category;

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
public class CategoryBestFoodHistoryResponse extends PublishableHistoryResponse {
    private UUID id;
    private UUID categoryPageId;
    private UUID foodId;
    private String description;
}
