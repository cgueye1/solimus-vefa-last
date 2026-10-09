package com.wakana.vefa.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.wakana.vefa.services.MinioService;

public class FileTraferUtil {

    // ⚠️ Injecté statiquement (voir plus bas)
    private static MinioService minioService;

    // Injecter MinioService une seule fois
    public static void setMinioService(MinioService service) {
        minioService = service;
    }

    // ✅ API IDENTIQUE
    public static String handleFileUpload(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return minioService.uploadFile(file);
        }
        return "";
    }

    // ✅ API IDENTIQUE
    public static List<String> uploadPictures(List<MultipartFile> pictures) throws IOException {
        List<String> fileNames = new ArrayList<>();

        if (pictures != null && !pictures.isEmpty()) {
            for (MultipartFile picture : pictures) {
                if (picture != null && !picture.isEmpty()) {
                    fileNames.add(minioService.uploadFile(picture));
                }
            }
        }
        return fileNames;
    }
}