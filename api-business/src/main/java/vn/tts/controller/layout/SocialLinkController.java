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
import vn.tts.model.response.layout.SocialLinkResponse;
import vn.tts.service.layout.SocialLinkService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/social-link")
@RequiredArgsConstructor
@Tag(name = "Social Link", description = "Luồng lấy thông tin Social Link")
public class SocialLinkController {
    private final SocialLinkService socialLinkService;

    @Operation(summary = "Lấy tất cả các link mạng xã hội")
    @GetMapping
    public ResponseEntity<ResponseBase<List<SocialLinkResponse>>> getAll() {
        return ResponseBase.success(socialLinkService.getAll());
    }
}
