package vn.tts.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.CategoryBestFoodResponse;
import vn.tts.proxy.category.CategoryBestFoodProxy;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryBestFoodService {
    private final CategoryBestFoodProxy categoryBestFoodProxy;

    public List<CategoryBestFoodResponse> getCategoryBestFoodResponses(UUID categoryPageId) {
        return categoryBestFoodProxy.cacheGetCategoryBestFoodResponses(categoryPageId);
    }
}
