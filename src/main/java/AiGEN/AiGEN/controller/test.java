package AiGEN.AiGEN.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class test {

    @PostMapping("/test")
    public String test() {
        return "test";
    }
}
