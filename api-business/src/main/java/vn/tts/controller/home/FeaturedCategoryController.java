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
import vn.tts.model.response.home.FeaturedCategoryResponse;
import vn.tts.service.home.FeaturedCategoryService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/featured-category")
@RequiredArgsConstructor
@Tag(name = "Featured Category", description = "Luồng lấy thông tin Featured Category")
public class FeaturedCategoryController {
    private final FeaturedCategoryService homeMainBannerService;

    @Operation(
            summary = "Lấy 2 featured categories mới nhất theo ngày xuất bản",
            description = "Lấy 2 featured categories mới nhất theo ngày xuất bản, " +
                          "nếu danh mục không được xuất bản thì sẽ lấy cái xuất bản tiếp theo"
    )
    @GetMapping
    public ResponseEntity<ResponseBase<List<FeaturedCategoryResponse>>> getFeaturedCategoryResponses() {
        return ResponseBase.success(homeMainBannerService.getFeaturedCategoryResponses());
    }
}