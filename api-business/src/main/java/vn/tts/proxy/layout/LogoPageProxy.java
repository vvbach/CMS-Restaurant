package vn.tts.proxy.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.layout.LogoPageEntity;
import vn.tts.model.response.layout.LogoPageResponse;
import vn.tts.repository.layout.LogoPageRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogoPageProxy {
    private final LogoPageRepository logoPageRepository;
    private final ServiceUtil serviceUtil;
    private final MinioService minioService;

    @CachePut(cacheNames = "logo_page", key = "'logo_page'")
    public LogoPageResponse cachePutLogoPageResponse(LogoPageEntity entity) {
        return getResponse(entity);
    }

    @Cacheable(cacheNames = "logo_page", key = "'logo_page'")
    public LogoPageResponse cacheGetLatestLogoPageResponse() {
        LogoPageEntity entity = logoPageRepository.findByLatestPublicationDate()
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("logo.page.not.found")));

        return getResponse(entity);
    }

    private LogoPageResponse getResponse(LogoPageEntity entity) {
        LogoPageResponse res = new LogoPageResponse(entity.getName(), entity.getUrl());

        try {
            res.setUrl(minioService.getPreSignedUrl(res.getUrl()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
        }

        return res;
    }
}
