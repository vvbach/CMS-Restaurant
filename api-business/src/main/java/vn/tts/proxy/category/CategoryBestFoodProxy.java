package vn.tts.proxy.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.category.CategoryBestFoodResponse;
import vn.tts.repository.category.CategoryBestFoodRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryBestFoodProxy {
    private final CategoryBestFoodRepository categoryBestFoodRepository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;

    @CacheEvict(cacheNames = "category_best_food", key = "#p0")
    public void cacheEvictCategoryBestFoodResponses(UUID categoryPageId) {
    }

    @Cacheable(cacheNames = "category_best_food", key = "#p0")
    public List<CategoryBestFoodResponse> cacheGetCategoryBestFoodResponses(UUID categoryPageId) {
        List<CategoryBestFoodResponse> responses = categoryBestFoodRepository.getResponsesByCategoryPageId(categoryPageId);

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
