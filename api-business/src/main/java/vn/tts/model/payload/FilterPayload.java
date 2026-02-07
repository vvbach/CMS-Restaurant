package vn.tts.model.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FilterPayload {
    private String categoryName;
    private Integer minPrice;
    private Integer maxPrice;
    private String searchQuery;
}
