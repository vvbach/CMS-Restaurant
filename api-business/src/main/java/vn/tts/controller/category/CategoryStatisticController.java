package vn.tts.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.category.CategoryStatisticResponse;
import vn.tts.service.category.CategoryStatisticService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-statistic")
@RequiredArgsConstructor
@Tag(name = "Category Statistic", description = "Luồng lấy thông tin Category Statistic")
public class CategoryStatisticController {
    private final CategoryStatisticService categoryStatisticService;

    @Operation(summary = "Lấy thông tin Category Statistic theo id Category Page mới nhất theo thời gian publish")
    @GetMapping("/{categoryPageId}")
    public ResponseEntity<ResponseBase<List<CategoryStatisticResponse>>> getCategoryStatisticResponses(
            @PathVariable(name = "categoryPageId") UUID categoryPageId,
            @RequestParam(name = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ResponseBase.success(categoryStatisticService.getCategoryStatisticResponses(categoryPageId, quantity));
    }
}
