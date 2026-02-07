package vn.tts.model.response.home;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeBestFoodResponse implements Serializable {
    private UUID id;
    private UUID foodId;
    private String description;
    private String foodName;
    private String foodDescription;
    private String imageUrl;
}
