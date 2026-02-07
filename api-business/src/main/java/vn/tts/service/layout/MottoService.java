package vn.tts.service.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.layout.MottoResponse;
import vn.tts.proxy.layout.MottoProxy;

@Slf4j
@Service
@RequiredArgsConstructor
public class MottoService {
    private final MottoProxy mottoProxy;

    public MottoResponse getLatest() {
        return mottoProxy.cacheGetLatestMottoResponse();
    }
}
