package ru.stepanov.model_service.main;

import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import ru.stepanov.model_service.utils.Schedule;
import ru.stepanov.model_service.service.ModelPreparer;

@SpringBootApplication
@Controller
public class SpringProjectApplication {

    private String scheduleFileName = "scheduleFile";

    public static void main(String[] args) {
        SpringApplication.run(SpringProjectApplication.class, args);
    }

    @PostMapping(value = "/service3/create-statistics")
    public ResponseEntity<HttpStatus> createAndSendStatistics(@RequestBody JSONObject nothing) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/service2/get-json-schedule-by-name?file-name=" + scheduleFileName;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String stringSchedule = responseEntity.getBody();
        ModelPreparer modelPreparer = new ModelPreparer(Schedule.getScheduleListFromString(stringSchedule));
        String statistics = ModelPreparer.toString(modelPreparer);
        url = "http://localhost:8082/service2/post-statistics";
        ResponseEntity<HttpStatus> statusResponseEntity = restTemplate
                .postForEntity(url, statistics, HttpStatus.class);
        if (statusResponseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<HttpStatus>(HttpStatus.OK, HttpStatus.OK);
    }
}
