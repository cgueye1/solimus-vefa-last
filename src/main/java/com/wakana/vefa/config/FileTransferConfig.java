
package com.wakana.vefa.config;

import org.springframework.context.annotation.Configuration;

import com.wakana.vefa.services.MinioService;
import com.wakana.vefa.util.FileTraferUtil;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileTransferConfig {

    private final MinioService minioService;

    public FileTransferConfig(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostConstruct
    public void init() {
        FileTraferUtil.setMinioService(minioService);
    }
}