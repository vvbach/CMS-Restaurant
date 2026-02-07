package vn.tts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.AboutPageResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.service.AboutPageService;

@Validated
@RestController
@RequestMapping("/v1/api/about-page")
@RequiredArgsConstructor
@Tag(name = "About Page", description = "Luồng lấy thông tin trang About")
public class AboutPageController {
    private final AboutPageService aboutPageService;

    @Operation(summary = "Lấy thông tin trang About mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<AboutPageResponse>> getLatest() {
        return ResponseBase.success(aboutPageService.getLatest());
    }
}
