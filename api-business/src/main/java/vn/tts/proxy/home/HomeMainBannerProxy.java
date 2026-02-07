package vn.tts.proxy.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.home.HomeMainBannerResponse;
import vn.tts.repository.home.HomeMainBannerRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HomeMainBannerProxy {
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;
    private final HomeMainBannerRepository repository;

    @Cacheable(cacheNames = "home_main_banner", key = "'home_main_banner'")
    public List<HomeMainBannerResponse> cacheGetHomeMainBannerResponses() {
        List<HomeMainBannerResponse> responses = repository.getHomeMainBannerResponses();

        responses.parallelStream().forEach(res -> {
            try {
                res.setImageUrl(minioService.getPresignedUrl(res.getImageUrl()));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
            }
        });

        return responses;
    }
}
