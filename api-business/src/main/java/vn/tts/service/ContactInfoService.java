package vn.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.ContactInfoResponse;
import vn.tts.proxy.ContactInfoProxy;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactInfoService {
    private final ContactInfoProxy contactInfoProxy;

    public ContactInfoResponse getLatest() {
        return contactInfoProxy.cacheGetLatestContactInfoResponse();
    }
}
