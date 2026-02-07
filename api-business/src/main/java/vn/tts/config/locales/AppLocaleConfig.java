package vn.tts.config.locales;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class AppLocaleConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeConfig() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH); // Đặt ngôn ngữ mặc định
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CustomLocaleInterceptor());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        // Thêm các converter khác nếu cần thiết
    }

    @Override
    public void configureContentNegotiation(@NotNull ContentNegotiationConfigurer configurer) {
        WebMvcConfigurer.super.configureContentNegotiation(configurer);

        configurer.defaultContentType(MediaType.ALL);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","HEAD")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
