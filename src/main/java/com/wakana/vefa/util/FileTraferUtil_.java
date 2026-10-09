package com.wakana.vefa.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class FileTraferUtil_ {
    
    
    
    private static final String fileUploadDirectory = "./files/";

    // Handle single file upload
    public static String handleFileUpload(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            File uploadDirectory = new File(fileUploadDirectory);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }
            String localFilePath = fileUploadDirectory + fileName;
            Files.copy(file.getInputStream(), Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);
            // Transfer file to remote server
            String remoteFilePath = "/ter/" + fileName;
            transferFileToRemote(localFilePath, remoteFilePath);
            return fileName;
        }
        return "";
    }

    // Upload multiple pictures
    public static List<String> uploadPictures(List<MultipartFile> pictures) throws IOException {
        List<String> pictureUrls = new ArrayList<>();
        
        if (pictures != null && !pictures.isEmpty()) {
            for (MultipartFile picture : pictures) {
                if (picture != null && !picture.isEmpty()) {
                    String fileName = handleFileUpload(picture);
                    if (fileName != null) {
                        pictureUrls.add(fileName);
                    }
                }
            }
        }
        return pictureUrls;
    }

    // Generate unique file names
    public static String generateUniqueFileName(String originalFilename) {
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString();
        return uniqueFileName + (fileExtension != null ? "." + fileExtension : "");
    }

    
     public static void transferFileToRemote(String localFilePath, String remoteFilePath) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession("root", "94.176.182.149", 22);
            session.setPassword("MGe21bc203b6feb4a38bc5a5daa7f540a3");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try (InputStream inputStream = new FileInputStream(localFilePath)) {
                channelSftp.put(inputStream, remoteFilePath);
            }
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace(); // Gérer les exceptions correctement dans votre application
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
