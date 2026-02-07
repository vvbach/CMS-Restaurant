package vn.tts.model.response.category;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBestFoodResponse implements Serializable {
    private UUID id;
    private UUID categoryPageId;
    private UUID foodId;
    private String description;
    private String foodName;
    private String foodDescription;
    private String imageUrl;
}
