package vn.tts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ContactInfoResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.service.ContactInfoService;

@Validated
@RestController
@RequestMapping("/v1/api/contact-info")
@RequiredArgsConstructor
@Tag(name = "Contact Info", description = "Luồng lấy thông tin trang Contact")
public class ContactInfoController {
    private final ContactInfoService contactInfoService;

    @Operation(summary = "Lấy thông tin trang Contact mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<ContactInfoResponse>> getLatest() {
        return ResponseBase.success(contactInfoService.getLatest());
    }
}
