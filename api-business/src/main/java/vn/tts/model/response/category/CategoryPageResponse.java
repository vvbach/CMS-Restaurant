package vn.tts.model.response.category;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPageResponse implements Serializable {
    private UUID id;
    private UUID categoryId;
    private String name;
}
