package vn.tts.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.tts.model.response.ResponseBase;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalException {

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBase<String>> handleAllExceptions(Exception ex, WebRequest request) {
        log.error(((ServletWebRequest) request).getRequest().getRequestURI());
        log.error(ex.getMessage(), ex);
        return ResponseBase.failure(HttpStatus.INTERNAL_SERVER_ERROR, getMessage("http.500.system.busy.data"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void ignoreStaticResourceNotFound(NoResourceFoundException ex) { }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void ignoreNoHandler(NoHandlerFoundException ex) { }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<ResponseBase<String>> handleAccessDeniedException(Exception ex, WebRequest request) {
        log.warn("Access denied at URI: {}", ((ServletWebRequest) request).getRequest().getRequestURI());
        return ResponseBase.failure(HttpStatus.FORBIDDEN, getMessage("exception.access.denied"));
    }

    @ExceptionHandler({
            UsernameNotFoundException.class,
            LoginFailedException.class
    })
    public ResponseEntity<ResponseBase<String>> handleAuthExceptions(Exception ex, WebRequest request) {
        return ResponseBase.failure(HttpStatus.UNAUTHORIZED, getMessage(ex.getMessage()));
    }

    @ExceptionHandler({
            AppBadRequestException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            SQLException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<?> handleBadRequestExceptions(Exception ex, WebRequest request) {
        Map<String, String> data = new HashMap<>();
        log.warn("Bad request at URI: {}", ((ServletWebRequest) request).getRequest().getRequestURI(), ex);

        if (ex instanceof AppBadRequestException appBad) {
            data.put(appBad.getFieldName(), appBad.getMessage());
            return ResponseBase.failure(HttpStatus.BAD_REQUEST, data);
        }

        if (ex instanceof ConstraintViolationException constraintViolation) {
            constraintViolation.getConstraintViolations().forEach(error -> {
                String path = error.getPropertyPath().toString();
                String field = path.contains(".") ? path.substring(path.indexOf(".") + 1) : path;
                data.put(field, error.getMessage());
            });
            return ResponseBase.failure(HttpStatus.BAD_REQUEST, data);
        }

        if (ex instanceof MissingServletRequestParameterException missingParam) {
            data.put(missingParam.getParameterName(), missingParam.getMessage());
            return ResponseBase.failure(HttpStatus.BAD_REQUEST, data);
        }

        return ResponseBase.failure(HttpStatus.BAD_REQUEST, getMessage("http.400.incorrect.data"));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<ResponseBase<Map<String, String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> data = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> data.put(error.getField(), error.getDefaultMessage()));
        return ResponseBase.failure(HttpStatus.BAD_REQUEST, data);
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (NoSuchMessageException ex) {
            log.debug("Missing message for code: {}", code);
        }
        return code;
    }
}
