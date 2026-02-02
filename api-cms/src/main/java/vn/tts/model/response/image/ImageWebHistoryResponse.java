package vn.tts.model.response.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageWebHistoryResponse extends PublishableHistoryResponse {
    private String description;
    private String pathImage;
}
