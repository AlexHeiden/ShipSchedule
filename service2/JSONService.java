package ru.stepanov.springproject.service2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.stepanov.springproject.service1.CargoType;
import ru.stepanov.springproject.service1.ScheduleElement;
import ru.stepanov.springproject.service1.Time;
import ru.stepanov.springproject.service3.ModelPreparer;
import ru.stepanov.springproject.service3.ScheduleElementKeeper;

import java.util.*;

public class JSONService
{
    public static final String scheduleFileName = System.getProperty("user.dir") + "/scheduleFile.json";

    private static LinkedList<ScheduleElement> scheduleList;

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

    public static JSONObject toJSON(ModelPreparer modelPreparer) {
        JSONObject obj = new JSONObject();
        obj.put("numberOfUnloadedShips", modelPreparer.getOverallNumberOfUnloadedShips());
        obj.put("averageQueueLength", modelPreparer.getAverageQueueLength());
        obj.put("averageQueueWaitInHours", modelPreparer.getAverageQueueWaitInHours());
        obj.put("maxUnloadingDelayInHours", modelPreparer.getMaxUnloadingDelayInHours());
        obj.put("averageUnloadingDelayInHours", modelPreparer.getAverageUnloadingDelayInHours());
        obj.put("overallFineValue", modelPreparer.getOverallFineValue());
        obj.put("requiredNumberOfContainerCranes", modelPreparer.getRequiredNumberOfContainerCranes());
        obj.put("requiredNumberOfLooseCranes", modelPreparer.getRequiredNumberOfLooseCranes());
        obj.put("requiredNumberOfLiquidCranes", modelPreparer.getRequiredNumberOfLiquidCranes());

        LinkedList<ScheduleElementKeeper> unloadedShipsList = modelPreparer.getOverallListOfUnloadedShips();
        JSONArray array = new JSONArray();
        for (ScheduleElementKeeper cargo: unloadedShipsList) {
            JSONObject jsonCargo = new JSONObject();
            jsonCargo.put("name", cargo.getName());
            jsonCargo.put("arrivingTime", toJSON(cargo.getActualArrivingTime()));
            jsonCargo.put("waitingForStartOfUnloadingTime", toJSON(cargo.getWaitForStartOfUnloadingTime()));
            jsonCargo.put("startUnloadingTime", toJSON(cargo.getStartUnloadingTime()));
            jsonCargo.put("unloadingDuration", toJSON(cargo.getUnloadingDuration()));
            array.add(jsonCargo);
        }

        obj.put("unloadedShipsList", array);
        return obj;
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
