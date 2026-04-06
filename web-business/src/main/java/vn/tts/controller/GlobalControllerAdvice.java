package vn.tts.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${api.hostname}")
    private String apiHostname;

    @Value("${api.port}")
    private String apiPort;

    @ModelAttribute("apiHostname")
    public String getApiHostname() {
        return apiHostname;
    }

    @ModelAttribute("apiPort")
    public String getApiPort() {
        return apiPort;
    }
}
