package vn.tts.model.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerFooterPublishResponse implements Serializable {
    private UUID id;
    private String title;
    private List<PromotedFoodPublishResponse> promotedFoodResponseList;
}
