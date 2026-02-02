package vn.tts.model.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatisticResponse extends PublishableResponse {
    private UUID id;
    private UUID categoryPageId;
    private UUID categoryId;
    private String name;
    private String description;
    private String imageUrl;
}
