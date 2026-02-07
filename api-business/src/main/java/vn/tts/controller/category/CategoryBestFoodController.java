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
import vn.tts.model.response.category.CategoryBestFoodResponse;
import vn.tts.service.category.CategoryBestFoodService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-best-food")
@RequiredArgsConstructor
@Tag(name = "Category Best Food", description = "Luồng lấy thông tin Best Food trang Category")
public class CategoryBestFoodController {
    private final CategoryBestFoodService categoryBestFoodService;

    @Operation(summary = "Lấy 12 Best Food trang Category mới nhất theo thời gian publish")
    @GetMapping("/{categoryPageId}")
    public ResponseEntity<ResponseBase<List<CategoryBestFoodResponse>>> getLatest(@PathVariable("categoryPageId") UUID categoryPageId) {
        return ResponseBase.success(categoryBestFoodService.getCategoryBestFoodResponses(categoryPageId));
    }
}
