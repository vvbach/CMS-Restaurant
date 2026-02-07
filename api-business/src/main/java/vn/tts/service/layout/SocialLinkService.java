package vn.tts.service.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.layout.SocialLinkResponse;
import vn.tts.proxy.layout.SocialLinkProxy;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLinkService {
    private final SocialLinkProxy socialLinkProxy;

    public List<SocialLinkResponse> getAll() {
        return socialLinkProxy.cacheGetSocialLinkResponses();
    }
}
