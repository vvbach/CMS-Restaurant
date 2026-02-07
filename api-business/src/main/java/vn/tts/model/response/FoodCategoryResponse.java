package vn.tts.model.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodCategoryResponse {
    private UUID id;
    private String name;
    private String description;
}
