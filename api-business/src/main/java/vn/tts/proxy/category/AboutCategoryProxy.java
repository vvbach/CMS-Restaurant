package vn.tts.proxy.category;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.category.AboutCategoryEntity;
import vn.tts.model.response.category.AboutCategoryResponse;
import vn.tts.repository.category.AboutCategoryRepository;
import vn.tts.service.ServiceUtil;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AboutCategoryProxy {
    private final AboutCategoryRepository aboutCategoryRepository;
    private final ServiceUtil serviceUtil;

    @Cacheable(cacheNames = "about_category", key = "#p0")
    public AboutCategoryResponse cacheGetAboutCategoryResponse(UUID categoryPageId) {
        AboutCategoryEntity entity = aboutCategoryRepository.getLatestByCategoryPageId(categoryPageId);

        if (entity == null)
            throw new RuntimeException(serviceUtil.getMessage("about.category.not.found"));

        return new AboutCategoryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getDescription()
        );
    }

    @CachePut(cacheNames = "about_category", key = "#p0.categoryPageId")
    public AboutCategoryResponse cachePutAboutCategoryResponse(AboutCategoryEntity entity) {
        return new AboutCategoryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getDescription()
        );
    }

    @CacheEvict(cacheNames = "about_category", key = "#p0")
    public void cacheEvictAboutCategoryResponse(UUID categoryPageId) {
    }
}
