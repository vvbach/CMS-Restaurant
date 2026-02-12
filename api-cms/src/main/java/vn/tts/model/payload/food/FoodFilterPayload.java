package vn.tts.model.payload.food;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import vn.tts.model.payload.FilterPayload;

@Getter
@Setter
public class FoodFilterPayload extends FilterPayload {
    @Schema(description = "Description với 500 ký tự ", example = "Test")
    private String categoryName;
}
