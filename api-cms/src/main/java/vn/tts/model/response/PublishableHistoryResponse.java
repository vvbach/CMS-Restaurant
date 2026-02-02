package vn.tts.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tts.enums.ContentHistoryStatus;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublishableHistoryResponse implements Serializable {
    private Instant eventDate;
    private ContentHistoryStatus status;
    private String reasonDelete;
    private String reasonRejection;
    private String reasonUnpublish;
}
