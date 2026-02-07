package vn.tts.proxy.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.home.FeaturedCategoryResponse;
import vn.tts.repository.home.FeaturedCategoryRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeaturedCategoryProxy {
    private final FeaturedCategoryRepository repository;
    private final ServiceUtil serviceUtil;
    private final MinioService minioService;

    @Cacheable(cacheNames = "featured_category", key = "'featured_category'")
    public List<FeaturedCategoryResponse> cacheGetFeaturedCategoryResponses() {
        List<FeaturedCategoryResponse> responses = repository.getFeaturedCategoryResponses();

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
