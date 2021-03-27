package service2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import service1.CargoType;
import service1.Schedule;
import service1.ScheduleElement;
import service1.Time;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class JSONService
{
    public static final String scheduleFileName = System.getProperty("user.dir") + "/scheduleFile.json";

    public static void getScheduleForModel(int numberOfShips,
                                           double minWeight,
                                           double maxWeight)
    {
        LinkedList<ScheduleElement> scheduleList = new Schedule(numberOfShips,
            minWeight,
            maxWeight).getScheduleElementList();

        JSONObject keeper = toJSON(scheduleList);
        try
        {
            FileWriter fileWriter = new FileWriter(scheduleFileName);
            fileWriter.write(keeper.toJSONString());
            fileWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static JSONObject toJSON(LinkedList<ScheduleElement> scheduleList)
    {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ScheduleElement scheduleElement: scheduleList)
        {
            JSONObject temp = new JSONObject();
            temp.put("arrivingTime", toJSON(scheduleElement.getArrivingTime()));
            temp.put("name", scheduleElement.getName());
            temp.put("cargoType", scheduleElement.getCargoType().toString());
            temp.put("weight", scheduleElement.getWeight());
            temp.put("unloadingTime", toJSON(scheduleElement.getUnloadingTime()));
            array.add(temp);
        }

        obj.put("scheduleList", array);
        return obj;
    }

    private static JSONObject toJSON(Time time)
    {
        JSONObject obj = new JSONObject();
        obj.put("day", time.getDay());
        obj.put("hour", time.getHour());
        obj.put("minute", time.getMinute());
        return obj;
    }

    public static LinkedList<ScheduleElement> getScheduleListFromJSON()
    {
        try
        {
            JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader(scheduleFileName));
            LinkedList<ScheduleElement> scheduleList = new LinkedList<ScheduleElement>();
            JSONArray array = (JSONArray) obj.get("scheduleList");
            for (int i = 0; i < array.size(); i++) {
                JSONObject temp = (JSONObject) array.get(i);
                Time arrivingTime = getTimeFromJSON((JSONObject) temp.get("arrivingTime"));
                String name = temp.get("name").toString();

                CargoType cargoType;
                String cargoName = temp.get("cargoType").toString();
                if (cargoName == CargoType.CONTAINER.toString()) {
                    cargoType = CargoType.CONTAINER;
                } else if (cargoName == CargoType.LOOSE.toString()) {
                    cargoType = CargoType.LOOSE;
                } else {
                    cargoType = CargoType.LIQUID;
                }

                double weight = Double.parseDouble(temp.get("weight").toString());
                Time unloadingTime = getTimeFromJSON((JSONObject) temp.get("unloadingTime"));

                scheduleList.add(new ScheduleElement(arrivingTime, name, cargoType, weight, unloadingTime));
            }
            return scheduleList;
        }
        catch (ParseException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return new LinkedList<ScheduleElement>();
    }

    public static Time getTimeFromJSON(JSONObject obj)
    {
        return new Time(Integer.parseInt(obj.get("day").toString()),
                Integer.parseInt(obj.get("hour").toString()),
                Integer.parseInt(obj.get("minute").toString()));
    }
}
