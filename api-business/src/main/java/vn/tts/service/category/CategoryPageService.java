package vn.tts.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.CategoryPageResponse;
import vn.tts.proxy.category.CategoryPageProxy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryPageService {
    private final CategoryPageProxy categoryPageProxy;

    public List<CategoryPageResponse> getCategoryPageResponses(int quantity) {
        return categoryPageProxy.cacheGetLatestCategoryPageResponse(quantity);
    }
}
