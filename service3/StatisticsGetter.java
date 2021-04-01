package service3;

import service1.Time;

import java.util.LinkedList;

public class StatisticsGetter implements Runnable{

    public final static int numberOfCargoThreadLaunches = 1000;
    public final static int additionalCraneFine = 30000;
    public final static int defaultNumberOfCranes = 1;

    private LinkedList<ScheduleElementKeeper> listForCargoThread;
    private LinkedList<ScheduleElementKeeper> finalListOfUnloadedCargos;
    private int numberOfCranes;
    private double fineValue;
    private int queueLength;
    private int numberOfQueueEvents;

    public StatisticsGetter(LinkedList<ScheduleElementKeeper> list) {
        listForCargoThread = new LinkedList<>(list);
        numberOfCranes = 0;
        fineValue = 0;
    }

    public void run() {
        if (listForCargoThread.isEmpty()) {
            queueLength = 0;
            numberOfQueueEvents = 0;
            finalListOfUnloadedCargos = new LinkedList<ScheduleElementKeeper>();
        } else {
            double oldFineValue = 1;

            while (fineValue < oldFineValue) {
                oldFineValue = fineValue;
                queueLength = 0;
                numberOfQueueEvents = 0;
                numberOfCranes++;
                for (int i = 0; i < numberOfCargoThreadLaunches; i++) {
                    CargoThread cargoThread = new CargoThread(listForCargoThread, numberOfCranes);
                    Thread thread = new Thread(cargoThread);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    fineValue += getFineValue(cargoThread.getArrivedCargoList(), numberOfCranes);
                    queueLength += cargoThread.getQueueLength();
                    numberOfQueueEvents += cargoThread.getNumberOfQueueEvents();
                    finalListOfUnloadedCargos = cargoThread.getUnloadedCargoList();
                }

                fineValue /= numberOfCargoThreadLaunches;
            }

            fineValue = oldFineValue;
            numberOfCranes--;
        }
    }

    public double getOverallFine() {
        try {
            if (numberOfCranes == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return fineValue;
    }

    public int getOverallQueueLength() {
        try {
            if (numberOfCranes == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return queueLength;
    }

    public int getOverallNumberOfQueueEvents() {
        try {
            if (numberOfCranes == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return numberOfQueueEvents;
    }

    public int getFinalNumberOfCranes() {
        try {
            if (numberOfCranes == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return numberOfCranes;
    }

    public LinkedList<ScheduleElementKeeper> getListForCargoThread() {
        try {
            if (numberOfCranes == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return finalListOfUnloadedCargos;
    }

    private long getFineValue(LinkedList<ScheduleElementKeeper> cargoList, int numberOfCranes) {
        long fine = (numberOfCranes - defaultNumberOfCranes) * additionalCraneFine;

        for (ScheduleElementKeeper cargo: cargoList) {
            long theoreticalFinishTimeInMinutes = cargo.getScheduleElement().getArrivingTime().getTimeInMinutes()
                    + cargo.getScheduleElement().getUnloadingTime().getTimeInMinutes();
            long actualFinishTimeInMinutes = cargo.getFinishUnloadingTime().getTimeInMinutes();
            long delayUnloadingMinutes = actualFinishTimeInMinutes = theoreticalFinishTimeInMinutes;

            if (delayUnloadingMinutes >= 0) {
                fine += delayUnloadingMinutes / (Time.maxMinute + 1);
            }
        }

        return fine;
    }
}
