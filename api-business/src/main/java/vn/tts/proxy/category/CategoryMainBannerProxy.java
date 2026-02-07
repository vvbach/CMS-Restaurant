package vn.tts.proxy.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.category.CategoryMainBannerResponse;
import vn.tts.repository.category.CategoryMainBannerRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryMainBannerProxy {
    private final CategoryMainBannerRepository categoryMainBannerRepository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;

    @CacheEvict(cacheNames = "category_main_banner", key = "#p0")
    public void cacheEvictCategoryMainBannerResponse(UUID categoryPageId) {
    }

    @Cacheable(cacheNames = "category_main_banner", key = "#p0")
    public List<CategoryMainBannerResponse> cacheGetCategoryMainBannerResponses(UUID categoryPageId) {
        List<CategoryMainBannerResponse> responses = categoryMainBannerRepository.getResponsesByCategoryPageId(categoryPageId);

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
