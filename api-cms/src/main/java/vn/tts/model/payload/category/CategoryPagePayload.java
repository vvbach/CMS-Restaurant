package vn.tts.model.payload.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPagePayload implements Serializable {
    @NotBlank(message = "{validate.article.description.not.blank}")
    private String description;

    @NotNull(message = "{validate.category.page.categoryId.not.null}")
    private UUID categoryId;
}
