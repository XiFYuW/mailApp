package com.base;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {

    private final ReadFileService readFileService;

    public MailController(ReadFileService readFileService) {
        this.readFileService = readFileService;
    }

    @GetMapping(value = "/getServerHost")
    public MailApi getServerHost(){
        return readFileService.sendApi();
    }
}
