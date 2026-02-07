package vn.tts.service.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.home.FeaturedCategoryResponse;
import vn.tts.proxy.home.FeaturedCategoryProxy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturedCategoryService {
    private final FeaturedCategoryProxy featuredCategoryProxy;

    public List<FeaturedCategoryResponse> getFeaturedCategoryResponses() {
        return featuredCategoryProxy.cacheGetFeaturedCategoryResponses();
    }
}