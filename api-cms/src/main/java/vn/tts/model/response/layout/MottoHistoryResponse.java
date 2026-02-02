package vn.tts.model.response.layout;

import lombok.*;
import vn.tts.model.response.PublishableHistoryResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MottoHistoryResponse extends PublishableHistoryResponse {
    private UUID id;
    private String title;
    private String description;
}
