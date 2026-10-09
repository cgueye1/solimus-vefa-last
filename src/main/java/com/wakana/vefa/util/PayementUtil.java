package com.wakana.vefa.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


@Component
public class PayementUtil {
   public static void payer() {
        String paymentRequestUrl = "https://paytech.sn/api/payment/request-payment";

        Map<String, Object> params = new HashMap<>();
        params.put("item_name", "Iphone 7");
        params.put("item_price", "200");
        params.put("currency", "XOF");
        params.put("ref_command", "HBZZYZVUZZZV");
        params.put("command_name", "Paiement Iphone 7 Gold via PayTech");
        params.put("env", "test");
        /*params.put("ipn_url", "https://domaine.com/ipn");
        params.put("success_url", "https://domaine.com/success");
        params.put("cancel_url", "https://domaine.com/cancel");*/
        Map<String, String> customField = new HashMap<>();
        customField.put("custom_fiel1", "value_1");
        customField.put("custom_fiel2", "value_2");
        params.put("custom_field", customField);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("API_KEY", "4f22f352239395d396072f6f24a3754e1df854f7532dc408735af60558c03f3f");
        headers.put("API_SECRET", "32f40bd8c5d25d0987b772018d74fce1d73a94b19a3301ec6335a9b07ded1816");

        try {
            URL url = new URL(paymentRequestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("API_KEY", "4f22f352239395d396072f6f24a3754e1df854f7532dc408735af60558c03f3f");
            conn.setRequestProperty("API_SECRET", "32f40bd8c5d25d0987b772018d74fce1d73a94b19a3301ec6335a9b07ded1816");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(new ObjectMapper().writeValueAsString(params).getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
