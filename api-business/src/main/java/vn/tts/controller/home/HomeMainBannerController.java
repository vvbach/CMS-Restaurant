package vn.tts.controller.home;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.home.HomeMainBannerResponse;
import vn.tts.service.home.HomeMainBannerService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/home-main-banner")
@RequiredArgsConstructor
@Tag(name = "Home Main Banner", description = "Luồng lấy thông tin Home Main Banner")
public class HomeMainBannerController {
    private final HomeMainBannerService homeMainBannerService;

    @Operation(
            summary = "Lấy 3 banner mới nhất theo ngày xuất bản",
            description = "Lấy 3 banner mới nhất theo ngày xuất bản, " +
                          "nếu food không được xuất bản thì sẽ lấy cái xuất bản tiếp theo"
    )
    @GetMapping
    public ResponseEntity<ResponseBase<List<HomeMainBannerResponse>>> getHomeMainBannerResponses() {
        return ResponseBase.success(homeMainBannerService.getHomeMainBannerResponses());
    }
}
