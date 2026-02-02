package vn.tts.model.event;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SendEmailEvent implements Serializable {
    private UUID entityId;
    private String entityType;
    private String action;
    private List<String> email;
    private String message;
    private String timestamp;

    @JsonCreator
    public SendEmailEvent(
            @JsonProperty("entityId") UUID entityId,
            @JsonProperty("entityType") String entityType,
            @JsonProperty("action") String action,
            @JsonProperty("email") List<String> email,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") String timestamp
    ) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.email = email;
        this.message = message;
        this.timestamp = timestamp;
    }
}