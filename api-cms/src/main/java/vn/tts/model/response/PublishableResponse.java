package vn.tts.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.tts.enums.ContentStatus;
import vn.tts.enums.DeleteEnum;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PublishableResponse implements Serializable {
    private ContentStatus status;
    private DeleteEnum isDelete;
    private String createdByName;
    private Instant createdAt;
    private String updatedByName;
    private Instant updatedAt;
    private String deletionReason;
    private String rejectionReason;
    private String unpublishReason;
}