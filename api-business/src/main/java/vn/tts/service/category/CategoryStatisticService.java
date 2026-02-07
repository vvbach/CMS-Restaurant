package vn.tts.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.CategoryStatisticResponse;
import vn.tts.proxy.category.CategoryStatisticProxy;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryStatisticService {
    private final CategoryStatisticProxy categoryStatisticProxy;

    public List<CategoryStatisticResponse> getCategoryStatisticResponses(UUID categoryPageId, Integer quantity) {
        return categoryStatisticProxy.cacheGetCategoryStatisticResponses(categoryPageId, quantity);
    }
}
