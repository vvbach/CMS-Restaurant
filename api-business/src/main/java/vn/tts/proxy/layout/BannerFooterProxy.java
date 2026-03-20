package vn.tts.proxy.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.layout.BannerFooterResponse;
import vn.tts.repository.food.FoodRepository;
import vn.tts.service.MinioService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BannerFooterProxy {
    private final FoodRepository foodRepository;
    private final MinioService minioService;

    @Cacheable(cacheNames = "banner_footer", key = "'banner_footer_quantity-' + #p0")
    public List<BannerFooterResponse> cacheGetBannerFooterResponses(int quantity) {
        List<BannerFooterResponse> responses = foodRepository.getBannerFooterResponses(quantity);

        responses.parallelStream().forEach(res -> {
            try {
                res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        });

        return responses;
    }
}
