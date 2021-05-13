package ru.stepanov.springproject.service3;

import ru.stepanov.springproject.service1.Time;

import java.util.LinkedList;

public class StatisticsGetter implements Runnable{

    public final static int numberOfCargoThreadLaunches = 1000;
    public final static int additionalCraneFine = 30000;
    public final static int hourFine = 100;
    public final static int defaultNumberOfCranes = 1;

    private LinkedList<ScheduleElementKeeper> listForCargoThread;
    private LinkedList<ScheduleElementKeeper> finalListOfUnloadedCargos;
    private CargoThread cargoThread;
    private int numberOfCranes;
    private double fineValue;
    private int queueLength;
    private int numberOfQueueEvents;

    public StatisticsGetter(LinkedList<ScheduleElementKeeper> list) {
        listForCargoThread = new LinkedList<>(list);
        numberOfCranes = 0;
        fineValue = 0;
        queueLength = 0;
        numberOfQueueEvents = 0;
    }

    public void run() {
        if (listForCargoThread.isEmpty()) {
            queueLength = 0;
            numberOfQueueEvents = 0;
            finalListOfUnloadedCargos = new LinkedList<ScheduleElementKeeper>();
        } else {
            double oldFineValue = Integer.MAX_VALUE;

            while (fineValue <= oldFineValue) {
                numberOfCranes++;
                for (int i = 0; i < numberOfCargoThreadLaunches; i++) {
                    cargoThread = new CargoThread(
                            (LinkedList<ScheduleElementKeeper>) listForCargoThread.clone(),
                            numberOfCranes);
                    Thread thread = new Thread(cargoThread);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    fineValue += getFineValue(cargoThread);
                    fineValue += getFineValueFromCranes(numberOfCranes);
                    queueLength += cargoThread.getQueueLength();
                    numberOfQueueEvents += cargoThread.getNumberOfQueueEvents();
                }

                fineValue /= numberOfCargoThreadLaunches;

                if (numberOfCranes == 1 || fineValue <= oldFineValue) {
                    finalListOfUnloadedCargos = cargoThread.getUnloadedCargoList();

                    if (fineValue == 0) {
                        oldFineValue = 0;
                        numberOfCranes++;
                        break;
                    }

                    oldFineValue = fineValue;
                }
            }

            fineValue = oldFineValue;
            numberOfCranes--;
        }
    }

    public double getOverallFine() { return fineValue; }

    public int getOverallQueueLength() { return queueLength; }

    public int getOverallNumberOfQueueEvents() { return numberOfQueueEvents; }

    public int getFinalNumberOfCranes() { return numberOfCranes; }

    public LinkedList<ScheduleElementKeeper> getListForCargoThread() { return finalListOfUnloadedCargos; }

    private double getFineValue(CargoThread cargoThread) {
        double fine = 0;

        for (ScheduleElementKeeper cargo: cargoThread.getArrivedCargoList()) {
            long theoreticalFinishTimeInMinutes = Math.max(
                    cargo.getScheduleElement().getArrivingTime().getTimeInMinutes(),
                    cargo.getActualArrivingTime().getTimeInMinutes())
                    + cargo.getScheduleElement().getUnloadingTime().getTimeInMinutes();
            Time maxTime = new Time (31, 0, 0);
            long delayUnloadingMinutes = maxTime.getTimeInMinutes() - theoreticalFinishTimeInMinutes;

            if (delayUnloadingMinutes >= 0) {
                fine += (double)delayUnloadingMinutes / (double)(Time.maxMinute + 1) * (double)hourFine;
            }
        }

        for (ScheduleElementKeeper cargo: cargoThread.getUnloadedCargoList()) {
            long theoreticalFinishTimeInMinutes = Math.max(
                    cargo.getScheduleElement().getArrivingTime().getTimeInMinutes(),
                    cargo.getActualArrivingTime().getTimeInMinutes())
                    + cargo.getScheduleElement().getUnloadingTime().getTimeInMinutes();
            long actualFinishTimeInMinutes = cargo.getFinishUnloadingTime().getTimeInMinutes();
            long delayUnloadingMinutes = actualFinishTimeInMinutes - theoreticalFinishTimeInMinutes;

            if (delayUnloadingMinutes >= 0) {
                fine += (double)delayUnloadingMinutes / (double)(Time.maxMinute + 1) * (double)hourFine;
            }
        }

        return fine;
    }

    private long getFineValueFromCranes(int numberOfCranes) {
        return (numberOfCranes - defaultNumberOfCranes) * additionalCraneFine;
    }
}
