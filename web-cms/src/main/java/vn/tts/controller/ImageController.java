package vn.tts.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/web/image-web")
@Controller
public class ImageController {

    @GetMapping(value = {"/",""})
    public String index(Model model) {
        model.addAttribute("viewName", "image-web");
        return "image-web"; // map tới templates/index.html
    }

}
