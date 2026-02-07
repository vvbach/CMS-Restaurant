package vn.tts.proxy.layout;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.layout.MottoEntity;
import vn.tts.model.response.layout.MottoResponse;
import vn.tts.repository.layout.MottoRepository;
import vn.tts.service.ServiceUtil;

@Component
@RequiredArgsConstructor
public class MottoProxy {
    private final MottoRepository mottoRepository;
    private final ServiceUtil serviceUtil;

    @CachePut(cacheNames = "motto", key = "'motto'")
    public MottoResponse cachePutMottoResponse(MottoEntity entity) {
        return new MottoResponse(entity.getTitle(), entity.getDescription());
    }

    @Cacheable(cacheNames = "motto", key = "'motto'")
    public MottoResponse cacheGetLatestMottoResponse() {
        MottoEntity entity = mottoRepository.findByLatestPublicationDate()
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("motto.not.found")));

        return new MottoResponse(entity.getTitle(), entity.getDescription());
    }
}
