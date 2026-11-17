package com.wakana.vefa.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Stockage AWS S3 : actif uniquement quand storage.type=s3 (production).
 * Utilise le rôle IAM de l'instance EC2 : aucune clé d'accès.
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3Service implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3Service(
            @Value("${storage.bucket}") String bucketName,
            @Value("${storage.region:eu-west-3}") String region) {
        this.bucketName = bucketName;

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        logger.info("S3Service initialized for bucket: {} in region: {}", bucketName, region);
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = generateFileName(file.getOriginalFilename());
        try (InputStream input = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Envoi en flux : le fichier n'est pas chargé entièrement en mémoire
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(input, file.getSize()));

            logger.info("File uploaded successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedFileNames.add(uploadFile(file));
        }
        return uploadedFileNames;
    }

    @Override
    public InputStream getFile(String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            logger.error("Error retrieving file from S3: {}", fileName, e);
            throw new RuntimeException("Failed to retrieve file from S3", e);
        }
    }

    @Override
    public String getSignedUrl(String fileName, Duration duration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            logger.error("Error generating signed URL for file: {}", fileName, e);
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            logger.error("Error deleting file from S3: {}", fileName, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    @Override
    public boolean exists(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "s3";
    }

    /**
     * Même format de nom que MinioService (UUID-nomOriginal), pour des noms cohérents
     * entre dev et prod ; les caractères spéciaux sont remplacés.
     */
    private String generateFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return UUID.randomUUID() + "-" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}