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
        model.addAttribute("location", "index");
        return "index";
    }

    @GetMapping("/product")
    public String product(Model model) {
        model.addAttribute("location", "product");
        return "product";
    }

    @GetMapping("/product/detail/{id}")
    public String productDetail(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("location", "product");
        return "detail";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("location", "about");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("location", "contact");
        return "contact";
    }
}
