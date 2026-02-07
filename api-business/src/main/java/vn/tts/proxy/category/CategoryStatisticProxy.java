package vn.tts.proxy.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.CategoryStatisticResponse;
import vn.tts.repository.category.CategoryStatisticRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryStatisticProxy {
    private final CategoryStatisticRepository repository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;

    @Cacheable(cacheNames = "category_statistic", key = "#p0")
    public List<CategoryStatisticResponse> cacheGetCategoryStatisticResponses(UUID categoryPageId, Integer quantity) {
        List<CategoryStatisticResponse> responses = repository.getCategoryStatisticResponsesByCategoryPageId(categoryPageId, quantity);

        responses.parallelStream().forEach(res -> {
            try {
                res.setImageUrl(minioService.getPresignedUrl(res.getImageUrl()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
            }
        });

        return responses;
    }

    @CacheEvict(cacheNames = "category_statistic", key = "#p0")
    public void cacheEvictCategoryStatisticResponses(UUID id) {}
}
