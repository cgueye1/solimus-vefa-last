package com.wakana.vefa.util;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class QRCodeUtil {
    
    public static String generateQrCode(String input) {
        try {
            // Use SHA-256 to generate the hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            // Convert the hash to Base64 string to store as qrcode
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Handle appropriate exception
            return null; // Or throw a custom exception here
        }
    }
    public static String decodeQrCode(String qrCode) {
        try {
            // Decode Base64 string
            byte[] decodedBytes = Base64.getDecoder().decode(qrCode);
            // Use SHA-256 to generate the hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(decodedBytes);
            // Convert the hash bytes to a hexadecimal representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            // Convert hexadecimal representation to string
            return hexString.toString(); 
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Handle appropriate exception
            return null; // Or throw a custom exception here
        }
    }
}
