package vn.tts.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

@Getter
@Setter
public class ResponseBase<T> implements Serializable {
    private String code = "200";
    private String message = "Hành động thực hiện thành công";
    private T data;

    public ResponseBase(String message, String value, T object) {
        this.message = message;
        this.code = value;
        this.data = object;
    }

    public ResponseBase(T data) {
        this.data = data;
    }

    public static <T> ResponseEntity<ResponseBase<T>> success(T data) {
        return ResponseEntity.ok(new ResponseBase<T>(data));
    }

    public static ResponseEntity<ResponseBase<String>> failure(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus).body(new ResponseBase<>(message,  httpStatus.name(), null));
    }

    public static ResponseEntity<ResponseBase<String>> failure(HttpStatus httpStatus, String code, String message) {
        return ResponseEntity.status(httpStatus).body(new ResponseBase<String>(message, code, null));
    }

    public static <T> ResponseEntity<ResponseBase<T>> failure(HttpStatus httpStatus, String code, String message, T data) {
        return ResponseEntity.status(httpStatus).body(new ResponseBase<T>(message, code, data));
    }

    public static <T> ResponseEntity<ResponseBase<T>> failure(HttpStatus httpStatus, T data) {
        return ResponseEntity.status(httpStatus).body(new ResponseBase<T>(httpStatus.name(), httpStatus.name(), data));
    }

    public static <T> ResponseEntity<ResponseBase<T>> failure(HttpStatus httpStatus, String code, T data) {
        return ResponseEntity.status(httpStatus).body(new ResponseBase<T>(httpStatus.name(), code, data));
    }


}
