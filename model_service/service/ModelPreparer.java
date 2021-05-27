package ru.stepanov.model_service.service;

import ru.stepanov.model_service.utils.CargoType;
import ru.stepanov.model_service.utils.ScheduleElement;
import ru.stepanov.model_service.utils.Time;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class ModelPreparer {

    private LinkedList<ScheduleElement> listForJSON;
    private LinkedList<ScheduleElementKeeper> containerList;
    private LinkedList<ScheduleElementKeeper> looseList;
    private LinkedList<ScheduleElementKeeper> liquidList;
    private LinkedList<ScheduleElementKeeper> overallListOfUnloadedShips;
    private int overallNumberOfUnloadedShips;
    private int overallQueueLength;
    private int overallNumberOfQueueEvents;
    private double averageQueueLength;
    private double overallFineValue;
    private int requiredNumberOfContainerCranes;
    private int requiredNumberOfLooseCranes;
    private int requiredNumberOfLiquidCranes;
    private double averageQueueWaitInMinutes;
    private double averageUnloadingDelayInMinutes;
    private long maxUnloadingDelayInMinutes;
    private Comparator<ScheduleElementKeeper> comparator = (element1, element2)
            -> element1.getActualArrivingTime().
            compareTo(element2.getActualArrivingTime());

    public ModelPreparer(LinkedList<ScheduleElement> scheduleList) {
        listForJSON = new LinkedList<>(scheduleList);

        containerList = new LinkedList<ScheduleElementKeeper>();
        looseList = new LinkedList<ScheduleElementKeeper>();
        liquidList = new LinkedList<ScheduleElementKeeper>();

        for (ScheduleElement scheduleElement: listForJSON) {
            if (scheduleElement.getCargoType() == CargoType.CONTAINER)
            {
                containerList.add(new ScheduleElementKeeper(scheduleElement));
            }
            else if (scheduleElement.getCargoType() == CargoType.LOOSE)
            {
                looseList.add(new ScheduleElementKeeper(scheduleElement));
            }
            else
            {
                liquidList.add(new ScheduleElementKeeper(scheduleElement));
            }
        }

        LinkedList<Thread> listOfThreads = new LinkedList<>();
        StatisticsGetter containerStatistics = new StatisticsGetter(containerList);
        StatisticsGetter looseStatistics = new StatisticsGetter(looseList);
        StatisticsGetter liquidStatistics = new StatisticsGetter(liquidList);
        listOfThreads.add(new Thread(containerStatistics));
        listOfThreads.add(new Thread(looseStatistics));
        listOfThreads.add(new Thread(liquidStatistics));

        for (Thread thread: listOfThreads) {
            thread.start();
        }

        for (Thread thread: listOfThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        overallListOfUnloadedShips = new LinkedList<>();
        overallListOfUnloadedShips.addAll(containerStatistics.getListForCargoThread());
        overallListOfUnloadedShips.addAll(looseStatistics.getListForCargoThread());
        overallListOfUnloadedShips.addAll(liquidStatistics.getListForCargoThread());
        Collections.sort(overallListOfUnloadedShips, comparator);

        overallNumberOfUnloadedShips = overallListOfUnloadedShips.size();

        overallQueueLength = containerStatistics.getOverallQueueLength()
                + looseStatistics.getOverallQueueLength()
                + liquidStatistics.getOverallQueueLength();

        overallNumberOfQueueEvents = containerStatistics.getOverallNumberOfQueueEvents()
                + looseStatistics.getOverallNumberOfQueueEvents()
                + liquidStatistics.getOverallNumberOfQueueEvents();

        averageQueueLength = overallQueueLength / overallNumberOfQueueEvents;

        overallFineValue = containerStatistics.getOverallFine()
                + looseStatistics.getOverallFine()
                + liquidStatistics.getOverallFine();

        requiredNumberOfContainerCranes = containerStatistics.getFinalNumberOfCranes();
        requiredNumberOfLooseCranes = looseStatistics.getFinalNumberOfCranes();
        requiredNumberOfLiquidCranes = liquidStatistics.getFinalNumberOfCranes();

        averageQueueWaitInMinutes = 0;
        averageUnloadingDelayInMinutes = 0;
        maxUnloadingDelayInMinutes = 0;

        for (ScheduleElementKeeper cargo: overallListOfUnloadedShips) {
            double temp = cargo.getStartUnloadingTime().getTimeInMinutes()
                    - cargo.getActualArrivingTime().getTimeInMinutes();

            if (temp > 0)
            averageQueueWaitInMinutes += temp;

            long minutesOfArrivingValues = Math.max(cargo.getActualArrivingTime().getTimeInMinutes(),
                    cargo.getScheduleElement().getArrivingTime().getTimeInMinutes());
            long unloadingDelay = cargo.getFinishUnloadingTime().getTimeInMinutes()
                    - minutesOfArrivingValues
                    - cargo.getScheduleElement().getUnloadingTime().getTimeInMinutes();

            if (unloadingDelay > 0) {
                averageUnloadingDelayInMinutes += unloadingDelay;

                if (unloadingDelay > maxUnloadingDelayInMinutes) {
                    maxUnloadingDelayInMinutes = unloadingDelay;
                }
            }
        }

        averageQueueWaitInMinutes /= overallNumberOfUnloadedShips;
        averageUnloadingDelayInMinutes /= overallNumberOfUnloadedShips;
    }

    public static String toString(ModelPreparer modelPreparer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Number of unloaded ships:"
                + "\n" + modelPreparer.getOverallNumberOfUnloadedShips()
                + "\n" + "Average queue length:"
                + "\n" + modelPreparer.getAverageQueueLength()
                + "\n" + "Average queue wait in hours:"
                + "\n" + modelPreparer.getAverageQueueWaitInHours()
                + "\n" + "Max unloading delay in hours:"
                + "\n" + modelPreparer.getMaxUnloadingDelayInHours()
                + "\n" + "Average unloading delay in hours:"
                + "\n" + modelPreparer.getAverageUnloadingDelayInHours()
                + "\n" + "Overall fine value:"
                + "\n" + modelPreparer.getOverallFineValue()
                + "\n" + "Required number of container cranes:"
                + "\n" + modelPreparer.getRequiredNumberOfContainerCranes()
                + "\n" + "Required number of loose cranes:"
                + "\n" + modelPreparer.getRequiredNumberOfLooseCranes()
                + "\n" + "Required number of liquid cranes:"
                + "\n" + modelPreparer.getRequiredNumberOfLiquidCranes()
                + "\n" + "Unloaded ships' list:");

        for (ScheduleElementKeeper cargo: modelPreparer.getOverallListOfUnloadedShips()) {
            stringBuilder.append("\n" + "Name:"
                    + "\n" + cargo.getName()
                    + "\n" + "Arriving time:"
                    + "\n" + Time.toString(cargo.getActualArrivingTime())
                    + "\n" + "Waiting for start of unloading time:"
                    + "\n" + Time.toString(cargo.getWaitForStartOfUnloadingTime())
                    + "\n" + "Unloading start time:"
                    + "\n" + Time.toString(cargo.getStartUnloadingTime())
                    + "\n" + "Unloading duration"
                    + "\n" + Time.toString(cargo.getUnloadingDuration()));
        }

        return stringBuilder.toString();
    }

    public LinkedList<ScheduleElementKeeper> getOverallListOfUnloadedShips() {
        return overallListOfUnloadedShips;
    }

    public int getOverallNumberOfUnloadedShips() {
        return overallNumberOfUnloadedShips;
    }

    public double getAverageQueueLength() {
        return averageQueueLength;
    }

    public double getOverallFineValue() {
        return overallFineValue;
    }

    public int getRequiredNumberOfContainerCranes() {
        return requiredNumberOfContainerCranes;
    }

    public int getRequiredNumberOfLooseCranes() {
        return requiredNumberOfLooseCranes;
    }

    public int getRequiredNumberOfLiquidCranes() {
        return requiredNumberOfLiquidCranes;
    }

    public double getAverageQueueWaitInHours() {
        return averageQueueWaitInMinutes / (Time.maxMinute + 1);
    }

    public double getAverageUnloadingDelayInHours() {
        return averageUnloadingDelayInMinutes / (Time.maxMinute + 1);
    }

    public double getMaxUnloadingDelayInHours() {
        return maxUnloadingDelayInMinutes / (Time.maxMinute + 1);
    }
}
