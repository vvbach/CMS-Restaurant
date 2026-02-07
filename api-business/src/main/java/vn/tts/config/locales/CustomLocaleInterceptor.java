package vn.tts.config.locales;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

public class CustomLocaleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String lang = request.getHeader("Accept-Language"); // Lấy ngôn ngữ từ header
        if (lang != null && !lang.isEmpty()) {
            Locale locale = Locale.forLanguageTag(lang);
            LocaleContextHolder.setLocale(locale); // Cài đặt Locale vào context
        }
        return true; // Cho phép tiếp tục xử lý yêu cầu
    }
}
