package vn.tts.model.response.about;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AboutPageHistoryResponse extends PublishableHistoryResponse implements Serializable {
    private String title;
    private String text;
    private String imageUrl;
}
