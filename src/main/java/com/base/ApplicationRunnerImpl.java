package com.base;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner{

    private final ReadFileService readFileService;

    public ApplicationRunnerImpl(ReadFileService readFileService) {
        this.readFileService = readFileService;
    }

    @Override
    public void run(ApplicationArguments args) {
        readFileService.asyncSendMail();
    }
}
