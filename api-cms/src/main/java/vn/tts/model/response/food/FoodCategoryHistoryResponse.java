package vn.tts.model.response.food;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodCategoryHistoryResponse extends PublishableHistoryResponse implements Serializable {
    private String name;
    private String description;
}
