package vn.tts.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        return "index"; // map tới templates/index.html
    }

    @GetMapping("/image")
    public String imageWeb() {
        return "image_web";
    }

    @GetMapping("/product")
    public String product(Model model) {
        return "product";
    }

    @GetMapping("/product/detail/{id}")
    public String productDetail(@PathVariable("id") UUID id, Model model) {
        return "detail";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "contact";
    }

    @GetMapping("/category-page/{id}")
    public String categoryPage(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("id", id);
        return "category-page";
    }
}
