package vn.tts.model.response.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.model.response.PublishableHistoryResponse;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUnitHistoryResponse extends PublishableHistoryResponse implements Serializable {
    private UUID id;
    private String name;
    private String logoUrl;
}
