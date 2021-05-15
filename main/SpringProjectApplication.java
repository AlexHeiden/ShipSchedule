package ru.stepanov.springproject.main;

import ru.stepanov.springproject.service1.CargoType;
import ru.stepanov.springproject.service1.Schedule;
import ru.stepanov.springproject.service1.ScheduleElement;
import ru.stepanov.springproject.service1.Time;
import ru.stepanov.springproject.service2.JSONService;
import ru.stepanov.springproject.service3.ModelPreparer;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

@SpringBootApplication
@Controller
public class SpringProjectApplication {

    private String scheduleFileName = "scheduleFile";
    private String statisticsFileName = "statisticsFile";

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

    @PostMapping("/service2/create-string-schedule")
    public ResponseEntity<String> getScheduleForUser(@RequestParam("shipnumber") int numberOfShipsToStore,
                                                         @RequestParam("min-weight") double minWeight,
                                                         @RequestParam("max-weight") double maxWeight) {
        try {
            if ((minWeight <= 0) || (minWeight > maxWeight) || (numberOfShipsToStore < 1)) {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don't input invalid arguments");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/service1/get-schedule?shipnumber=" + numberOfShipsToStore
                + "&min-weight=" + minWeight + "&max-weight=" + maxWeight;
        String stringSchedule = restTemplate.getForEntity(url, String.class).getBody();
        LinkedList<ScheduleElement> scheduleList = Schedule.getScheduleListFromString(stringSchedule);
        JSONObject jsonSchedule = JSONService.toJSON(scheduleList);
        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/"
                    + scheduleFileName + ".json");
            fileWriter.write(jsonSchedule.toJSONString());
            fileWriter.close();
        }
        catch(IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown");
        }

        return new ResponseEntity<String>(stringSchedule, HttpStatus.OK);
    }

    @GetMapping("/service2/get-string-schedule")
    public ResponseEntity<String> getScheduleForUser() {
        try {
            JSONObject schedule = (JSONObject) new JSONParser().parse(new FileReader
                    (System.getProperty("user.dir") + "/" + scheduleFileName + ".json"));
            LinkedList<ScheduleElement> scheduleList = JSONService.getScheduleListFromJSON(schedule);
            return new ResponseEntity<String>(Schedule.toString(scheduleList), HttpStatus.OK);
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown." +
                    "This file probably doesn't exist.");
        }
        catch(ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Parse exception was thrown");
        }
    }

    @GetMapping("/service2/get-json-schedule-by-name")
    public ResponseEntity<String> getScheduleByName(@RequestParam("file-name") String fileName) {
        if (!fileName.equals(scheduleFileName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The name of schedule file is scheduleFile");
        }

        JSONObject schedule;
        try {
            schedule = (JSONObject) new JSONParser().parse(new FileReader
                    (System.getProperty("user.dir")
                    + "/" + fileName + ".json"));
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown." +
                    "This file probably doesn't exist.");
        }
        catch(ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Parse exception was thrown");
        }

        LinkedList<ScheduleElement> scheduleList = JSONService.getScheduleListFromJSON(schedule);
        String stringSchedule = Schedule.toString(scheduleList);
        return new ResponseEntity<String>(stringSchedule, HttpStatus.OK);
    }

    @GetMapping("/service2/get-statistics")
    public ResponseEntity<String> getStatistics() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/service3/create-statistics";
        ResponseEntity<HttpStatus> statusResponseEntity = restTemplate
                .postForEntity(url, new JSONObject(), HttpStatus.class);
        if (statusResponseEntity.getBody() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IO exception was thrown");
        }

        try {
            JSONObject schedule = (JSONObject) new JSONParser().parse(new FileReader
                    (System.getProperty("user.dir") + "/" + statisticsFileName + ".json"));
            String statistics = JSONService.JSONToModelString(schedule);
            return new ResponseEntity<String>(statistics, HttpStatus.OK);
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown." +
                    "This file probably doesn't exist.");
        }
        catch(ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Parse exception was thrown");
        }
    }

    @PostMapping(value = "/service2/post-statistics")
    public ResponseEntity<HttpStatus> postStatistics(@RequestBody String statistics) {
        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/"
                    + statisticsFileName + ".json");
            fileWriter.write(JSONService.modelStringToJSON(statistics).toJSONString());
            fileWriter.close();
        }
        catch(IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
    }

    @PostMapping(value = "/service2/post-new-ship", consumes = "application/json", produces = "application/json")
    public ResponseEntity<HttpStatus> postNewShip(@RequestBody JSONObject jsonShip) {
        String name = jsonShip.get("name").toString();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        }

        Time arrivingTime;
        try {
            arrivingTime = new Time(jsonShip.get("arrivingTime").toString());
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time");
        }

        CargoType cargoType;
        String cargoName = jsonShip.get("cargoType").toString();
        if (cargoName.equals(CargoType.CONTAINER.toString())) {
            cargoType = CargoType.CONTAINER;
        } else if (cargoName.equals(CargoType.LOOSE.toString())) {
            cargoType = CargoType.LOOSE;
        } else if (cargoName.equals(CargoType.LOOSE.toString())){
            cargoType = CargoType.LIQUID;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cargo type");
        }

        double weight;
        try {
            weight = Double.parseDouble(jsonShip.get("weight").toString());
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid weight");
        }

        JSONObject schedule;
        try {
            schedule = (JSONObject) new JSONParser().parse(new FileReader
                    (System.getProperty("user.dir")
                            + "/" + scheduleFileName + ".json"));
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown");
        }
        catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ParseException was thrown");
        }
        JSONArray array = (JSONArray) schedule.get("scheduleList");
        JSONObject scheduleElement = JSONService.toJSON(new ScheduleElement(arrivingTime, name, cargoType, weight));
        array.add(scheduleElement);
        schedule.put("scheduleList", array);

        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir")
                    + "/" + scheduleFileName + ".json");
            fileWriter.write(schedule.toJSONString());
            fileWriter.close();
        }
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException was thrown");
        }

        return new ResponseEntity<>(HttpStatus.OK, HttpStatus.OK);
    }

    @PostMapping(value = "/service3/create-statistics", consumes = "application/json", produces = "application/json")
    public ResponseEntity<HttpStatus> createAndSendStatistics(@RequestBody JSONObject nothing) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/service2/get-json-schedule-by-name?file-name=" + scheduleFileName;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String stringSchedule = responseEntity.getBody();
        ModelPreparer modelPreparer = new ModelPreparer(Schedule.getScheduleListFromString(stringSchedule));
        String statistics = ModelPreparer.toString(modelPreparer);
        url = "http://localhost:8080/service2/post-statistics";
        ResponseEntity<HttpStatus> statusResponseEntity = restTemplate
                .postForEntity(url, statistics, HttpStatus.class);
        if (statusResponseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<HttpStatus>(HttpStatus.OK, HttpStatus.OK);
    }
}
