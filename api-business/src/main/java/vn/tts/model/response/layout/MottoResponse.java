package vn.tts.model.response.layout;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MottoResponse implements Serializable {
    private String title;
    private String description;
}
