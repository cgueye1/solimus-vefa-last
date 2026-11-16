package com.wakana.vefa.services;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Common interface for file storage across MinIO (dev) and AWS S3 (prod)
 */
public interface StorageService {
    
    /**
     * Upload a single file
     * @param file File to upload
     * @return File name/key in storage
     */
    String uploadFile(MultipartFile file);
    
    /**
     * Upload multiple files
     * @param files List of files to upload
     * @return List of file names/keys in storage
     */
    List<String> uploadMultipleFiles(List<MultipartFile> files);
    
    /**
     * Get file as stream
     * @param fileName File name/key
     * @return InputStream of the file
     */
    InputStream getFile(String fileName);
    
    /**
     * Get signed/pre-signed URL for file download
     * @param fileName File name/key
     * @param duration Duration of URL validity
     * @return Signed URL for download
     */
    String getSignedUrl(String fileName, Duration duration);
    
    /**
     * Delete a file
     * @param fileName File name/key
     */
    void deleteFile(String fileName);
    
    /**
     * Check if file exists
     * @param fileName File name/key
     * @return true if exists, false otherwise
     */
    boolean exists(String fileName);
    
    /**
     * Get storage type (minio or s3)
     * @return Storage type name
     */
    String getStorageType();
}
