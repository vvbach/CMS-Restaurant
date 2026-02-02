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
public class AboutCategoryHistoryResponse extends PublishableHistoryResponse {
    private UUID id;
    private UUID categoryPageId;
    private String title;
    private String subtitle;
    private String description;
}
