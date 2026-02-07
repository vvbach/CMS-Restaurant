package vn.tts.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.CategoryMainBannerResponse;
import vn.tts.proxy.category.CategoryMainBannerProxy;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryMainBannerService {
    private final CategoryMainBannerProxy categoryMainBannerProxy;

    public List<CategoryMainBannerResponse> getCategoryMainBanner(UUID categoryPageId) {
        return categoryMainBannerProxy.cacheGetCategoryMainBannerResponses(categoryPageId);
    }
}
