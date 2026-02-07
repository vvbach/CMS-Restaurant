package vn.tts.service.home;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.home.HomeMainBannerResponse;
import vn.tts.proxy.home.HomeMainBannerProxy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeMainBannerService {
    private final HomeMainBannerProxy homeMainBannerProxy;

    public List<HomeMainBannerResponse> getHomeMainBannerResponses() {
        return homeMainBannerProxy.cacheGetHomeMainBannerResponses();
    }
}
