package ru.stepanov.model_service.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Schedule {
    private int numberOfShipsToStore;
    private double minWeight;
    private double maxWeight;
    private int numberOfShipsInSchedule = 0;
    private String beginningOfShipName = "Ship ";
    private int firstDay = 1;
    private int lastDay = 30;
    private LinkedList<ScheduleElement> scheduleElementList = new LinkedList();
    private Comparator<ScheduleElement> comparator = (element1, element2)
            -> element1.getArrivingTime().
            compareTo(element2.getArrivingTime());

    public static final int containerUnitUnloadingPerHour = 1;
    public static final int looseUnitUnloadingPerHour = 1;
    public static final int liquidUnitUnloadingPerHour = 1;

    public Schedule(int numberOfShipsToStore, double minWeight, double maxWeight) {

        try {
            if ((minWeight <= 0) || (minWeight > maxWeight) || (numberOfShipsToStore < 1))
            {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        this.numberOfShipsToStore = numberOfShipsToStore;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;

        for (int i = 0; i < numberOfShipsToStore; i++) {
            Time time = Time.getRandomTime(Time.maxDay);

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


            scheduleElementList.add(new ScheduleElement(time, name, cargoType, weight));
        }

        Collections.sort(scheduleElementList, comparator);
    }

    public LinkedList<ScheduleElement> getScheduleElementList() { return scheduleElementList; }

    public static String toString(Schedule schedule) { return Schedule.toString(schedule.getScheduleElementList()); }

    public static String toString(LinkedList<ScheduleElement> scheduleList) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(scheduleList.size());
        for (ScheduleElement scheduleElement : scheduleList) {
            stringBuilder.append("\n" + scheduleElement.getName()
                    + "\n" + scheduleElement.getCargoType().toString()
                    + "\n" + scheduleElement.getWeight()
                    + "\n" + Time.toString(scheduleElement.getArrivingTime()));
        }

        return stringBuilder.toString();
    }

    public static LinkedList<ScheduleElement> getScheduleListFromString(String string) {
        LinkedList<ScheduleElement> scheduleList = new LinkedList<>();
        String[] scheduleListElements = string.split("\n");
        int numberOfScheduleElementParts = 4;

        int size = Integer.parseInt(scheduleListElements[0]);
        for (int i = 0; i < size * numberOfScheduleElementParts; i += numberOfScheduleElementParts) {
            String name = scheduleListElements[i + 1];

            CargoType cargoType;
            String cargoName = scheduleListElements[i + 2];
            if (cargoName.equals(CargoType.CONTAINER.toString())) {
                cargoType = CargoType.CONTAINER;
            } else if (cargoName.equals(CargoType.LOOSE.toString())) {
                cargoType = CargoType.LOOSE;
            } else {
                cargoType = CargoType.LIQUID;
            }

            double weight = Double.parseDouble(scheduleListElements[i + 3]);
            Time arrivingTime = new Time(scheduleListElements[i + 4]);

            scheduleList.add(new ScheduleElement(arrivingTime, name, cargoType, weight));
        }

        return scheduleList;
    }
}
