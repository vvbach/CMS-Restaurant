package vn.tts.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.category.CategoryMainBannerResponse;
import vn.tts.service.category.CategoryMainBannerService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-main-banner")
@RequiredArgsConstructor
@Tag(name = "Category Main Banner", description = "Luồng lấy thông tin Category Main Banner")
public class CategoryMainBannerController {
    private final CategoryMainBannerService categoryMainBannerService;

    @Operation(
            summary = "Lấy 3 banner mới nhất theo ngày xuất bản",
            description = "Lấy 3 banner thuộc danh mục mới nhất theo ngày xuất bản, " +
                          "nếu food không được xuất bản thì sẽ lấy cái xuất bản tiếp theo"
    )
    @GetMapping("/{categoryPageId}")
    public ResponseEntity<ResponseBase<List<CategoryMainBannerResponse>>>
    getCategoryMainBanner(@PathVariable(value = "categoryPageId") UUID categoryPageId) {
        return ResponseBase.success(categoryMainBannerService.getCategoryMainBanner(categoryPageId));
    }
}
