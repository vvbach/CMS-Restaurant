package vn.tts.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tts.model.response.category.AboutCategoryResponse;
import vn.tts.proxy.category.AboutCategoryProxy;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AboutCategoryService {
    private final AboutCategoryProxy aboutCategoryProxy;

    public AboutCategoryResponse getAboutCategoryResponse(UUID categoryPageId) {
        return aboutCategoryProxy.cacheGetAboutCategoryResponse(categoryPageId);
    }
}
