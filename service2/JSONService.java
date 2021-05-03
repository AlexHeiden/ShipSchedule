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
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Scanner;

public class JSONService
{
    public static final String scheduleFileName = System.getProperty("user.dir") + "/scheduleFile.json";

    private static LinkedList<ScheduleElement> scheduleList;

    public JSONService(int numberOfShips,
                       double minWeight,
                       double maxWeight)
    {
        scheduleList = new Schedule(numberOfShips,
                minWeight,
                maxWeight).getScheduleElementList();
    }

    public void getScheduleForModel()
    {
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

    public static void createNewShips() {
        System.out.println("Would you like to add ships into schedule? y/n ");
        Scanner in = new Scanner(System.in);
        try {
            String acception = in.nextLine();
            if (acception.equals("y")) {
                System.out.println("How many ships would you like to input: ");

                int numberOfShips = in.nextInt();
                if (numberOfShips <= 0) {
                    throw new InputMismatchException();
                }

                for (int i = 0; i < numberOfShips; i++) {
                    in.nextLine();
                    System.out.println("Input ship's name: ");
                    String name = in.nextLine();
                    if (name.isEmpty()) {
                        throw new InputMismatchException();
                    }

                    System.out.println("Input ship's arriving time in the format dd:hh:mm: ");
                    Time time = new Time(in.nextLine());

                    System.out.println("Choose cargo type of your ship (loose, liquid or container): ");
                    String cargoTypeName = in.nextLine();
                    if (cargoTypeName.isEmpty()) {
                        throw new InputMismatchException();
                    }

                    CargoType cargoType;
                    if (cargoTypeName.equals("loose")) {
                        cargoType = CargoType.LOOSE;
                    } else if (cargoTypeName.equals("liquid")) {
                        cargoType = CargoType.LIQUID;
                    } else if (cargoTypeName.equals("container")) {
                        cargoType = CargoType.CONTAINER;
                    } else {
                        throw new InputMismatchException();
                    }

                    System.out.println("Input weight of the cargo: ");
                    if (!in.hasNextDouble()) {
                        throw new InputMismatchException();
                    }
                    double weight = in.nextDouble();

                    scheduleList.add(new ScheduleElement(time, name, cargoType, weight));
                }
            }
        }
        catch (InputMismatchException e) {
            e.printStackTrace();
        }
        in.close();
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

    private JSONObject toJSON(LinkedList<ScheduleElement> scheduleList)
    {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ScheduleElement scheduleElement: scheduleList) {
            array.add(toJSON(scheduleElement));
        }

        obj.put("scheduleList", array);
        return obj;
    }

    private JSONObject toJSON(ScheduleElement scheduleElement)
    {
        JSONObject obj = new JSONObject();
        obj.put("arrivingTime", toJSON(scheduleElement.getArrivingTime()));
        obj.put("name", scheduleElement.getName());
        obj.put("cargoType", scheduleElement.getCargoType().toString());
        obj.put("weight", scheduleElement.getWeight());
        return obj;
    }

    private JSONObject toJSON(Time time)
    {
        JSONObject obj = new JSONObject();
        obj.put("day", time.getDay());
        obj.put("hour", time.getHour());
        obj.put("minute", time.getMinute());
        return obj;
    }
}
