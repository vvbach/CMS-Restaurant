package vn.tts.service.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.home.HomeBestFoodResponse;
import vn.tts.proxy.home.HomeBestFoodProxy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeBestFoodService {
    private final HomeBestFoodProxy homeBestFoodProxy;

    public List<HomeBestFoodResponse> getHomeBestFoodResponses() {
         return homeBestFoodProxy.cacheGetHomeBestFoodResponses();
    }
}
