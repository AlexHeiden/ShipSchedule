package service1;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Schedule
{
    private int numberOfShipsToStore;
    private double minWeight;
    private double maxWeight;
    private int numberOfShipsInSchedule = 0;
    private String beginningOfShipName = "Ship ";
    private int firstDay = 1;
    private int lastDay = 30;

    public static final int containerUnitUnloadingPerHour = 1;
    public static final int looseUnitUnloadingPerHour = 1;
    public static final int liquidUnitUnloadingPerHour = 1;

    LinkedList<ScheduleElement> scheduleElementList = new LinkedList();

    public Schedule(int numberOfShipsToStore, double minWeight, double maxWeight)
    {
        this.numberOfShipsToStore = numberOfShipsToStore;

        try
        {
            if ((minWeight <= 0) || (minWeight > maxWeight))
            {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        this.minWeight = minWeight;
        this.maxWeight = maxWeight;

        for (int i = 0; i < numberOfShipsToStore; i++)
        {
            Time time = Time.getRandomTime();

            numberOfShipsInSchedule++;
            String name = beginningOfShipName + numberOfShipsInSchedule;

            int numberOfCargoTypes = 3;
            CargoType cargoType;
            switch ((int) (Math.random() * numberOfCargoTypes)) {
                case 0: {
                    cargoType = CargoType.CONTAINER;
                    break;
                }
                case 1: {
                    cargoType = CargoType.LOOSE;
                    break;
                }
                default: {
                    cargoType = CargoType.LIQUID;
                    break;
                }
            }

            double weight = Math.random() * (maxWeight - minWeight)
                    + minWeight;

            Time unloadingTime = new Time(0, 0, 0);
            switch (cargoType)
            {
                case CONTAINER: {
                    weight = (int)(weight + 1);
                    unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / containerUnitUnloadingPerHour));
                    break;
                }
                case LOOSE: {
                    unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / looseUnitUnloadingPerHour));
                    break;
                }
                default: {
                    unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / liquidUnitUnloadingPerHour));
                    break;
                }
            }

            scheduleElementList.add(new ScheduleElement(time, name, cargoType, weight, unloadingTime));
        }
        Comparator<ScheduleElement> comparator = (element1, element2)
                -> element1.getArrivingTime().compareTo(element2.getArrivingTime());
        Collections.sort(scheduleElementList, comparator);
    }

    public LinkedList<ScheduleElement> getScheduleElementList() { return scheduleElementList; }

    public void printSchedule()
    {
        printSchedule(scheduleElementList);
    }

    public static void printSchedule(LinkedList<ScheduleElement> list)
    {
        for (ScheduleElement scheduleElement: list) {
            System.out.println(scheduleElement.getName());
            scheduleElement.getArrivingTime().printTime();
            System.out.println(scheduleElement.getCargoType().toString());
            System.out.println(scheduleElement.getWeight());
            scheduleElement.getUnloadingTime().printTime();
            System.out.println();
        }
    }
}
