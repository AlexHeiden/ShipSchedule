package ru.stepanov.springproject.main;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TestMain {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/service1/get-schedule-service1?shipnumber="
                + 5 + "&min-weight=" + 1 + "&max-weight=" + 6;
        ResponseEntity<JSONObject> responseEntity = restTemplate.getForEntity(url, JSONObject.class);
        System.out.println(responseEntity.getBody());
    }
}
