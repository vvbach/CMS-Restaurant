package vn.tts.model.response.layout;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BannerFooterResponse implements Serializable {
    private UUID foodId;
    private String imageUrl;
}
