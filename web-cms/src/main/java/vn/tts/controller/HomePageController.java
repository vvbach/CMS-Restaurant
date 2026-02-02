package vn.tts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {
    @GetMapping("/home-page/home-main-banner")
    public String homeMainBanner(Model model) {
        model.addAttribute("viewName", "home-page-home-main-banner");
        return "home-page/home-main-banner";
    }

    @GetMapping("/home-page/featured-category")
    public String featuredCategory(Model model) {
        model.addAttribute("viewName", "home-page-featured-category");
        return "home-page/featured-category";
    }

    @GetMapping("/home-page/home-best-food")
    public String homeBestFood(Model model) {
        model.addAttribute("viewName", "home-page-home-best-food");
        return "home-page/home-best-food";
    }
}