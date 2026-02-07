package vn.tts.model.response.category;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AboutCategoryResponse implements Serializable {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
}
