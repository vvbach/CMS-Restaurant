package vn.tts.service.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.layout.BannerFooterResponse;
import vn.tts.proxy.layout.BannerFooterProxy;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BannerFooterService {
    private final BannerFooterProxy bannerFooterProxy;

    public List<BannerFooterResponse> getBannerFooterResponses(int quantity) {
        return bannerFooterProxy.cacheGetBannerFooterResponses(quantity);
    }
}
