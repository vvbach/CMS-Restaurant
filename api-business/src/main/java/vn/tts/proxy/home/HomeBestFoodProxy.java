package vn.tts.proxy.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.model.response.home.HomeBestFoodResponse;
import vn.tts.repository.home.HomeBestFoodRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HomeBestFoodProxy {
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;
    private final HomeBestFoodRepository homeBestFoodRepository;

    @Cacheable(cacheNames = "home_best_food", key = "'home_best_food'")
    public List<HomeBestFoodResponse> cacheGetHomeBestFoodResponses() {
        List<HomeBestFoodResponse> responses = homeBestFoodRepository.getHomeBestFoodResponses();

        responses.parallelStream().forEach(res -> {
            try {
                res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
            }
        });

        return responses;
    }
}
