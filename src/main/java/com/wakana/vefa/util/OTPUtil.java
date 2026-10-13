

package com.wakana.vefa.util;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class OTPUtil {

    private static final long OTP_VALIDITY_MILLIS = 2 * 60 * 1000L;
  
    
    

    private Map<String, OtpInfo> otpStorage = new HashMap<>();

    public String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }


    public void storeOTP(String phoneNumber, String otp) {
        otpStorage.put(phoneNumber, new OtpInfo(otp, System.currentTimeMillis() + OTP_VALIDITY_MILLIS));
    }

    public boolean validateOTP(String phoneNumber, String otp) {
        OtpInfo otpInfo = otpStorage.get(phoneNumber);
        return otpInfo != null && otpInfo.getOtp().equals(otp) && otpInfo.getExpiryTime() >= System.currentTimeMillis();
    }

    private static class OtpInfo {
        private String otp;
        private long expiryTime;

        public OtpInfo(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
    

     
    
    
    public static String hmacSha(String secretKey, String value) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(value.getBytes("UTF-8"));
        
        StringBuilder hexResult = new StringBuilder();
        for (byte b : rawHmac) {
            hexResult.append(String.format("%02x", b));
        }
        return hexResult.toString();
    }
    
}
