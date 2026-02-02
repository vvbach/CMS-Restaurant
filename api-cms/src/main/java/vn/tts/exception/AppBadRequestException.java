package vn.tts.exception;

import lombok.Getter;

@Getter
public class AppBadRequestException extends RuntimeException {

    private final String fieldName;

    public AppBadRequestException(String field, String message) {
        super(message);
        this.fieldName = field;
    }

}
