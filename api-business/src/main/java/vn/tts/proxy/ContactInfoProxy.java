package vn.tts.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.ContactInfoEntity;
import vn.tts.model.response.ContactInfoResponse;
import vn.tts.repository.ContactInfoRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContactInfoProxy {
    private final ContactInfoRepository contactInfoRepository;
    private final ServiceUtil serviceUtil;
    private final MinioService minioService;

    @CachePut(cacheNames = "contact_info", key = "'contact_info'")
    public ContactInfoResponse cachePutContactInfoResponse(ContactInfoEntity entity) {
        return getResponse(entity);
    }

    @Cacheable(cacheNames = "contact_info", key = "'contact_info'")
    public ContactInfoResponse cacheGetLatestContactInfoResponse() {
        ContactInfoEntity entity = contactInfoRepository.findByLatestPublicationDate()
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("contact.info.not.found")));

        return getResponse(entity);
    }

    private ContactInfoResponse getResponse(ContactInfoEntity entity) {
        ContactInfoResponse res = new ContactInfoResponse(
                entity.getText(),
                entity.getImageUrl(),
                entity.getAddress(),
                entity.getEmail(),
                entity.getPhoneNumber()
        );

        try {
            res.setImageUrl(minioService.getPreSignedUrl(res.getImageUrl()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
        }

        return res;
    }
}
