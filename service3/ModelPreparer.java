package service3;

import service1.CargoType;
import service1.ScheduleElement;
import service1.Time;
import service2.JSONService;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class ModelPreparer {

    private LinkedList<ScheduleElement> listForJSON;
    private LinkedList<ScheduleElementKeeper> containerList = new LinkedList<ScheduleElementKeeper>();
    private LinkedList<ScheduleElementKeeper> looseList = new LinkedList<ScheduleElementKeeper>();
    private LinkedList<ScheduleElementKeeper> liquidList = new LinkedList<ScheduleElementKeeper>();
    private Comparator<ScheduleElementKeeper> comparator = (element1, element2)
            -> element1.getActualArrivingTime().
            compareTo(element2.getActualArrivingTime());

    public ModelPreparer() {
        listForJSON = JSONService.getScheduleListFromJSON();

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

        LinkedList<ScheduleElementKeeper> overallList = new LinkedList<>();
        overallList.addAll(containerStatistics.getListForCargoThread());
        overallList.addAll(looseStatistics.getListForCargoThread());
        overallList.addAll(liquidStatistics.getListForCargoThread());
        Collections.sort(overallList, comparator);

        int overallNumberOfUnloadedShips = overallList.size();

        int overallQueueLength = containerStatistics.getOverallQueueLength()
                + looseStatistics.getOverallQueueLength()
                + liquidStatistics.getOverallQueueLength();

        int overallNumberOfQueueEvents = containerStatistics.getOverallNumberOfQueueEvents()
                + looseStatistics.getOverallNumberOfQueueEvents()
                + liquidStatistics.getOverallNumberOfQueueEvents();

        double averageQueueLength = overallQueueLength / overallNumberOfQueueEvents;

        double overallFineValue = containerStatistics.getOverallFine()
                + looseStatistics.getOverallFine()
                + liquidStatistics.getOverallFine();

        double requiredNumberOfContainerCranes = containerStatistics.getFinalNumberOfCranes();
        double requiredNumberOfLooseCranes = looseStatistics.getFinalNumberOfCranes();
        double requiredNumberOfLiquidCranes = liquidStatistics.getFinalNumberOfCranes();

        double averageQueueWaitInMinutes = 0;
        double averageUnloadingDelayInMinutes = 0;
        long maxUnloadingDelayInMinutes = 0;

        for (ScheduleElementKeeper cargo: overallList) {
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

        System.out.println("The results of model work");
        System.out.println("Number of unloaded ships: " + overallNumberOfUnloadedShips);
        System.out.println("Average queue length: " + averageQueueLength);
        System.out.println("Average queue wait in hours: " + averageQueueWaitInMinutes
                / (Time.maxMinute + 1));
        System.out.println("Maximum unloading delay in hours: " + maxUnloadingDelayInMinutes
                / (Time.maxMinute + 1));
        System.out.println("Average unloading delay in hours: " + averageUnloadingDelayInMinutes
                / (Time.maxMinute + 1));
        System.out.println("Overall fine: " + overallFineValue);
        System.out.println("Required number of CONTAINER cranes: " + requiredNumberOfContainerCranes);
        System.out.println("Required number of LOOSE cranes: " + requiredNumberOfLooseCranes);
        System.out.println("Required number of LIQUID cranes: " + requiredNumberOfLiquidCranes);
        System.out.println();

        for (ScheduleElementKeeper cargo: overallList) {
            cargo.print();
        }
    }
}
