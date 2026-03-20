package vn.tts.proxy.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import vn.tts.entity.layout.AdminUnitEntity;
import vn.tts.model.response.layout.AdminUnitResponse;
import vn.tts.repository.layout.AdminUnitRepository;
import vn.tts.service.MinioService;
import vn.tts.service.ServiceUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminUnitProxy {
    private final AdminUnitRepository adminUnitRepository;
    private final ServiceUtil serviceUtil;
    private final MinioService minioService;

    @CachePut(cacheNames = "admin_unit", key = "'admin_unit'")
    public AdminUnitResponse cachePutAdminUnitResponse(AdminUnitEntity entity) {
        return getResponse(entity);
    }

    @Cacheable(cacheNames = "admin_unit", key = "'admin_unit'")
    public AdminUnitResponse cacheGetLatestAdminUnitResponse() {
        AdminUnitEntity entity = adminUnitRepository.findByLatestPublicationDate()
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("admin.unit.not.found")));

        return getResponse(entity);
    }

    private AdminUnitResponse getResponse(AdminUnitEntity entity) {
        AdminUnitResponse res = new AdminUnitResponse(entity.getName(), entity.getLogoUrl());

        try {
            res.setLogoUrl(minioService.getPreSignedUrl(res.getLogoUrl()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(serviceUtil.getMessage("minio.service.get.url.error"));
        }

        return res;
    }
}
