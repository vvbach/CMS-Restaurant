package vn.tts.model.response.food;

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
public class FoodCategoryResponse extends PublishableResponse {
    private UUID id;
    private String name;
    private String description;
}