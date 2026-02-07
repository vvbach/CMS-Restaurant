package vn.tts.controller.category;

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
import vn.tts.model.response.category.CategoryPageResponse;
import vn.tts.service.category.CategoryPageService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/category-page")
@RequiredArgsConstructor
@Tag(name = "Category Page", description = "Luồng lấy thông tin Category Page")
public class CategoryPageController {
    private final CategoryPageService categoryPageService;

    @Operation(summary = "Lấy thông tin Category Page mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<List<CategoryPageResponse>>> getLatest(
            @RequestParam(value = "quantity", required = false, defaultValue = "3") int quantity) {
        return ResponseBase.success(categoryPageService.getCategoryPageResponses(quantity));
    }
}
