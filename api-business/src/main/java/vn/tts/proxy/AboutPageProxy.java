package vn.tts.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.AboutPageEntity;
import vn.tts.model.response.AboutPageResponse;
import vn.tts.repository.AboutPageRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class AboutPageProxy {
    private final AboutPageRepository aboutPageRepository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;

    @CachePut(cacheNames = "about_page", key = "'about_page'")
    public AboutPageResponse cachePutAboutPageResponse(AboutPageEntity entity) {
        return getResponse(entity);
    }

    @Cacheable(cacheNames = "about_page", key = "'about_page'")
    public AboutPageResponse cacheGetLatestAboutPageResponse() {
        AboutPageEntity entity = aboutPageRepository.findByLatestPublicationDate()
                .orElseThrow(() -> new RuntimeException("about.page.not.found"));

        return getResponse(entity);
    }

    private AboutPageResponse getResponse(AboutPageEntity entity) {
        AboutPageResponse res = new AboutPageResponse(entity.getTitle(), entity.getText(), entity.getImageUrl());

        try {
            res.setImageUrl(minioService.getPresignedUrl(res.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
        }

        return res;
    }
}
