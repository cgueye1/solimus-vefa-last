
package com.wakana.vefa.config;

import org.springframework.context.annotation.Configuration;

import com.wakana.vefa.services.StorageService;
import com.wakana.vefa.util.FileTraferUtil;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileTransferConfig {

    private final StorageService minioService;

    public FileTransferConfig(StorageService minioService) {
        this.minioService = minioService;
    }

    @PostConstruct
    public void init() {
        FileTraferUtil.setStorageService(minioService);
    }
}