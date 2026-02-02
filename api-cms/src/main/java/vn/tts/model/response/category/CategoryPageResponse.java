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
public class CategoryPageResponse extends PublishableResponse {
    private UUID id;
    private String description;
    private UUID categoryId;
}
