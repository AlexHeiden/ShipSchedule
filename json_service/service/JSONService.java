package ru.stepanov.json_service.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.stepanov.json_service.utils.CargoType;
import ru.stepanov.json_service.utils.ScheduleElement;
import ru.stepanov.json_service.utils.Time;

import java.util.*;

public class JSONService
{
    public static LinkedList<ScheduleElement> getScheduleListFromJSON(JSONObject schedule) {
        LinkedList<ScheduleElement> scheduleList = new LinkedList<ScheduleElement>();
        JSONArray array = (JSONArray) schedule.get("scheduleList");
        for (int i = 0; i < array.size(); i++) {
            JSONObject temp = (JSONObject) array.get(i);
            Time arrivingTime = getTimeFromJSON((JSONObject) temp.get("arrivingTime"));
            String name = temp.get("name").toString();

            CargoType cargoType;
            String cargoName = temp.get("cargoType").toString();
            if (cargoName.equals(CargoType.CONTAINER.toString())) {
                cargoType = CargoType.CONTAINER;
            } else if (cargoName.equals(CargoType.LOOSE.toString())) {
                cargoType = CargoType.LOOSE;
            } else {
                cargoType = CargoType.LIQUID;
            }

            double weight = Double.parseDouble(temp.get("weight").toString());
            scheduleList.add(new ScheduleElement(arrivingTime, name, cargoType, weight));
        }

        return scheduleList;
    }

    public static JSONObject toJSON(LinkedList<ScheduleElement> scheduleList)
    {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ScheduleElement scheduleElement: scheduleList) {
            array.add(toJSON(scheduleElement));
        }

        obj.put("scheduleList", array);
        return obj;
    }

    public static JSONObject modelStringToJSON(String string) {
        JSONObject obj = new JSONObject();
        String[] statisticsElements = string.split("\n");
        obj.put("numberOfUnloadedShips", statisticsElements[1]);
        obj.put("averageQueueLength", statisticsElements[3]);
        obj.put("averageQueueWaitInHours", statisticsElements[5]);
        obj.put("maxUnloadingDelayInHours", statisticsElements[7]);
        obj.put("averageUnloadingDelayInHours", statisticsElements[9]);
        obj.put("overallFineValue", statisticsElements[11]);
        obj.put("requiredNumberOfContainerCranes", statisticsElements[13]);
        obj.put("requiredNumberOfLooseCranes", statisticsElements[15]);
        obj.put("requiredNumberOfLiquidCranes", statisticsElements[17]);

        JSONArray array = new JSONArray();
        int numberOfElementsInCargoString = 10;
        for (int i = 18;
             i < Integer.parseInt(statisticsElements[1]) * numberOfElementsInCargoString;
             i += numberOfElementsInCargoString) {
            JSONObject jsonCargo = new JSONObject();
            jsonCargo.put("name", statisticsElements[i + 2]);
            jsonCargo.put("arrivingTime", statisticsElements[i + 4]);
            jsonCargo.put("waitingForStartOfUnloadingTime", statisticsElements[i + 6]);
            jsonCargo.put("startUnloadingTime", statisticsElements[i + 8]);
            jsonCargo.put("unloadingDuration", statisticsElements[i + 10]);
            array.add(jsonCargo);
        }

        obj.put("unloadedShipsList", array);
        return obj;
    }

    public static String JSONToModelString(JSONObject obj) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Number of unloaded ships:"
                + "\n" + obj.get("numberOfUnloadedShips").toString()
                + "\n" + "Average queue length:"
                + "\n" + obj.get("averageQueueLength").toString()
                + "\n" + "Average queue wait in hours:"
                + "\n" + obj.get("averageQueueWaitInHours").toString()
                + "\n" + "Max unloading delay in hours:"
                + "\n" + obj.get("maxUnloadingDelayInHours").toString()
                + "\n" + "Average unloading delay in hours:"
                + "\n" + obj.get("averageUnloadingDelayInHours").toString()
                + "\n" + "Overall fine value:"
                + "\n" + obj.get("overallFineValue").toString()
                + "\n" + "Required number of container cranes:"
                + "\n" + obj.get("requiredNumberOfContainerCranes").toString()
                + "\n" + "Required number of loose cranes:"
                + "\n" + obj.get("requiredNumberOfLooseCranes").toString()
                + "\n" + "Required number of liquid cranes:"
                + "\n" + obj.get("requiredNumberOfLiquidCranes").toString()
                + "\n" + "Unloaded ships' list:");

        JSONArray array = (JSONArray)obj.get("unloadedShipsList");
        for (int i = 0; i < array.size(); i++) {
            JSONObject temp = (JSONObject) array.get(i);
            stringBuilder.append("\n" + "Name:"
                    + "\n" + temp.get("name").toString()
                    + "\n" + "Arriving time:"
                    + "\n" + temp.get("arrivingTime").toString()
                    + "\n" + "Waiting for start of unloading time:"
                    + "\n" + temp.get("waitingForStartOfUnloadingTime").toString()
                    + "\n" + "Unloading start time:"
                    + "\n" + temp.get("startUnloadingTime").toString()
                    + "\n" + "Unloading duration"
                    + "\n" + temp.get("unloadingDuration").toString());
        }

        return stringBuilder.toString();
    }

    public static JSONObject toJSON(ScheduleElement scheduleElement) {
        JSONObject obj = new JSONObject();
        obj.put("arrivingTime", toJSON(scheduleElement.getArrivingTime()));
        obj.put("name", scheduleElement.getName());
        obj.put("cargoType", scheduleElement.getCargoType().toString());
        obj.put("weight", scheduleElement.getWeight());
        return obj;
    }

    private static JSONObject toJSON(Time time) {
        JSONObject obj = new JSONObject();
        obj.put("day", time.getDay());
        obj.put("hour", time.getHour());
        obj.put("minute", time.getMinute());
        return obj;
    }

    private static Time getTimeFromJSON(JSONObject obj) {
        return new Time(Long.parseLong(obj.get("day").toString()),
                Long.parseLong(obj.get("hour").toString()),
                Long.parseLong(obj.get("minute").toString()));
    }
}
