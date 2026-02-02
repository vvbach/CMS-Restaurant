package vn.tts.model.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse <T> implements Serializable {
    private T data;
    private long total;
}
