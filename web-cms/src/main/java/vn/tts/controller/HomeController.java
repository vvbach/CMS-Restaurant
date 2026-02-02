package vn.tts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("viewName", "profile");
        return "profile";
    }

    @GetMapping("/force-change-password")
    public String forceChangePassword(Model model) {
        return "force-change-password";
    }

    @GetMapping({"/", "/admin"})
    public String index(Model model) {
        model.addAttribute("viewName", "view-admin");
        return "admin";
    }

    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("viewName", "view-roles");
        return "roles";
    }

    @GetMapping("/product")
    public String blog(Model model) {
        model.addAttribute("viewName", "product");
        return "product";
    }

    @GetMapping("/category")
    public String category(Model model) {
        model.addAttribute("viewName", "category");
        return "category";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("viewName", "contact");
        return "contact";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("viewName", "about");
        return "about";
    }

    @GetMapping("/send-email")
    public String sendEmail(Model model) {
        model.addAttribute("viewName", "send-email");
        return "send-email";
    }

    @GetMapping("*")
    public String error(Model model) {
        model.addAttribute("viewName", "");
        return "error";
    }
}
