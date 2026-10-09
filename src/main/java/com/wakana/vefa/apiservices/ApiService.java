package com.wakana.vefa.apiservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Service
public class ApiService {
    @Autowired
    private RestTemplate restTemplate;
    public ResponseEntity<?>  callExternalApi(String url) {
        ResponseEntity<?> response = restTemplate.getForEntity(url, String.class);
        return response;
    }
}
