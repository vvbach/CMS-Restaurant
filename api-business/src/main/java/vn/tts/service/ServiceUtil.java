package vn.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Component
public class ServiceUtil {
    private final MessageSource messageSource;

    public String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException ex) {
            log.error(ex.getMessage(), code);
        }
        return StringUtils.EMPTY;
    }
}
