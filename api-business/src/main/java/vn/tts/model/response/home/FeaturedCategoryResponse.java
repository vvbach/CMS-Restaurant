package vn.tts.model.response.home;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeaturedCategoryResponse implements Serializable {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private String imageUrl;
}
