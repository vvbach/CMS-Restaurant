package vn.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.AboutPageResponse;
import vn.tts.proxy.AboutPageProxy;

@Slf4j
@Service
@RequiredArgsConstructor
public class AboutPageService {
    private final AboutPageProxy aboutPageProxy;

    public AboutPageResponse getLatest() {
        return aboutPageProxy.cacheGetLatestAboutPageResponse();
    }
}
