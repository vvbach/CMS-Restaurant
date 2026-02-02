package vn.tts.model.payload;

import lombok.Getter;
import lombok.Setter;
import vn.tts.enums.ContentStatus;
import vn.tts.enums.DeleteEnum;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class FilterPayload implements Serializable {
    private DeleteEnum isDelete;
    private String description;
    private ContentStatus status;
    private String createdByName;
    private LocalDate formDate;
    private LocalDate toDate;
}
