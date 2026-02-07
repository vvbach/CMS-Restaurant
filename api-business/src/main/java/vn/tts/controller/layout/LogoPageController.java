package vn.tts.controller.layout;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.layout.LogoPageResponse;
import vn.tts.service.layout.LogoPageService;

@Validated
@RestController
@RequestMapping("/v1/api/logo-page")
@RequiredArgsConstructor
@Tag(name = "Logo Page", description = "Luồng lấy thông tin Logo Page")
public class LogoPageController {
    private final LogoPageService logoPageService;

    @Operation(summary = "Lấy thông tin Logo Page mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<LogoPageResponse>> getLatest() {
        return ResponseBase.success(logoPageService.getLatest());
    }
}
