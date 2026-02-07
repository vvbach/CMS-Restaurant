package vn.tts.service.food;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tts.model.response.FoodCategoryResponse;
import vn.tts.repository.food.FoodCategoryRepository;
import vn.tts.service.ServiceUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodCategoryService {
    private final FoodCategoryRepository foodCategoryRepository;
    private final ServiceUtil serviceUtil;

    public List<FoodCategoryResponse> findAll() {
        return foodCategoryRepository.getCategories();
    }

    public FoodCategoryResponse getResponseById(UUID id) {
        return foodCategoryRepository.getResponseById(id)
                .orElseThrow(() -> new RuntimeException(serviceUtil.getMessage("category.not.found")));
    }
}
