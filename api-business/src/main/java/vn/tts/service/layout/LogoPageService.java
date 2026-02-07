package vn.tts.service.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.layout.LogoPageResponse;
import vn.tts.proxy.layout.LogoPageProxy;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoPageService {
    private final LogoPageProxy logoPageProxy;

    public LogoPageResponse getLatest() {
        return logoPageProxy.cacheGetLatestLogoPageResponse();
    }
}
