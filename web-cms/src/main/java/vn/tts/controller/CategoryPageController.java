package vn.tts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class CategoryPageController {
    @GetMapping("/category-page")
    public String categoryPage(Model model) {
        model.addAttribute("viewName", "category-page");
        return "category-page/category-page";
    }

    @GetMapping("/category-page/{categoryPageId}/category-best-food")
    public String categoryBestFood(Model model, @PathVariable("categoryPageId") UUID categoryPageId) {
        model.addAttribute("viewName", "category-page-category-best-food");
        return "category-page/category-best-food";
    }
  
    @GetMapping("/category-page/{categoryPageId}/category-main-banner")
    public String categoryMainBanner(Model model, @PathVariable("categoryPageId") UUID id) {
        model.addAttribute("viewName", "category-page-category-main-banner");
        return "category-page/category-main-banner";
    }
 
    @GetMapping("/category-page/{categoryPageId}/about-category")
    public String aboutCategory(Model model, @PathVariable("categoryPageId") UUID categoryPageId) {
        model.addAttribute("viewName", "category-page-about-category");
        return "category-page/about-category";
    }
  
    @GetMapping("/category-page/{categoryPageId}/category-statistic")
    public String categoryStatistic(Model model, @PathVariable("categoryPageId") UUID categoryPageId) {
        model.addAttribute("viewName", "category-page-category-statistic");
        return "category-page/category-statistic";
    }
}
