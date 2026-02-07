package vn.tts.proxy.category;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.category.CategoryPageResponse;
import vn.tts.repository.category.CategoryPageRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryPageProxy {
    private final CategoryPageRepository categoryPageRepository;

    @Cacheable(cacheNames = "category_nav", key = "'category_nav_quantity-' + #p0")
    public List<CategoryPageResponse> cacheGetLatestCategoryPageResponse(int quantity) {
        return categoryPageRepository.getCategoryPageResponses(quantity);
    }
}
