package ru.stepanov.schedule_service.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.stepanov.schedule_service.service.Schedule;


@SpringBootApplication
@Controller
public class SpringProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringProjectApplication.class, args);
    }

    @GetMapping("/service1/get-schedule")
    public ResponseEntity<String> getSchedule(@RequestParam("shipnumber") int numberOfShipsToStore,
                                              @RequestParam("min-weight") double minWeight,
                                              @RequestParam("max-weight") double maxWeight) {
        return new ResponseEntity<String>
                (Schedule.toString(new Schedule(numberOfShipsToStore, minWeight, maxWeight)),
                        HttpStatus.OK);
    }
}
