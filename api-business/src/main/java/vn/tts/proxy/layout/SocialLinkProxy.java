package vn.tts.proxy.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.layout.SocialLinkEntity;
import vn.tts.model.response.layout.SocialLinkResponse;
import vn.tts.repository.layout.SocialLinkRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocialLinkProxy {
    private final SocialLinkRepository socialLinkRepository;
    private final MinioService minioService;
    private final ServiceUtil serviceUtil;

    @CachePut(cacheNames = "social_link", key = "'social_link'")
    public List<SocialLinkResponse> cachePutSocialLinkResponses(SocialLinkEntity entity) {
        // When using stream.toList (list created other way is fine),
        // This List is final and the serializer is not configured to handle this.
        // So try to remember to convert into non-final type in the future.
        List<SocialLinkResponse> tmp = new ArrayList<>(socialLinkRepository.getSocialLinks()
                .parallelStream()
                .map(e -> new SocialLinkResponse(e.getUrl(), e.getPlatform(), e.getIconUrl()))
                .toList());

        // Make List into a non-final type (see above comment)
        List<SocialLinkResponse> responses = new ArrayList<>(tmp);

        responses.parallelStream().forEach(response -> {
            try {
                response.setIconUrl(minioService.getPresignedUrl(response.getIconUrl()));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
            }
        });

        return responses;
    }

    @Cacheable(cacheNames = "social_link", key = "'social_link'")
    public List<SocialLinkResponse> cacheGetSocialLinkResponses() {
        List<SocialLinkResponse> tmp = socialLinkRepository.getSocialLinks()
                .parallelStream()
                .map(e -> new SocialLinkResponse(e.getUrl(), e.getPlatform(), e.getIconUrl()))
                .toList();

        // Make List into a non-final type (see above comment)
        List<SocialLinkResponse> responses = new ArrayList<>(tmp);

        responses.parallelStream().forEach(response -> {
            try {
                response.setIconUrl(minioService.getPresignedUrl(response.getIconUrl()));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
            }
        });

        return responses;
    }
}
