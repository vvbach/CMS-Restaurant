package vn.tts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController {
    @GetMapping("/layout-page/social-link")
    public String socialLink(Model model) {
        model.addAttribute("viewName", "layout-page-social-link");
        return "layout-page/social-link";
    }

    @GetMapping("/layout-page/logo-page")
    public String logoPage(Model model) {
        model.addAttribute("viewName", "layout-page-logo-page");
        return "layout-page/logo-page";
    }

    @GetMapping("/layout-page/admin-unit")
    public String adminUnit(Model model) {
        model.addAttribute("viewName", "layout-page-admin-unit");
        return "layout-page/admin-unit";
    }

    @GetMapping("/layout-page/motto")
    public String motto(Model model) {
        model.addAttribute("viewName", "layout-page-motto");
        return "layout-page/motto";
    }
}