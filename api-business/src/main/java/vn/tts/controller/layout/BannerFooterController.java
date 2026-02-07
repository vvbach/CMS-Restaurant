package vn.tts.controller.layout;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.layout.BannerFooterResponse;
import vn.tts.service.layout.BannerFooterService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/banner-footer")
@RequiredArgsConstructor
@Tag(name = "Banner Footer", description = "Luồng lấy thông tin Banner Footer")
public class BannerFooterController {
    private final BannerFooterService bannerFooterService;

    @Operation(summary = "Lấy trong mỗi danh mục món ăn, những món còn lại sẽ được lấy ngẫu nhiên")
    @GetMapping
    public ResponseEntity<ResponseBase<List<BannerFooterResponse>>> getLatest(
            @RequestParam(value = "quantity", required = false, defaultValue = "6") int quantity) {
        return ResponseBase.success(bannerFooterService.getBannerFooterResponses(quantity));
    }
}
