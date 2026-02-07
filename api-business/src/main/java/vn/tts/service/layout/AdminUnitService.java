package vn.tts.service.layout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.layout.AdminUnitResponse;
import vn.tts.proxy.layout.AdminUnitProxy;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUnitService {
    private final AdminUnitProxy adminUnitProxy;

    public AdminUnitResponse getLatest() {
        return adminUnitProxy.cacheGetLatestAdminUnitResponse();
    }
}
