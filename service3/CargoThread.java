package service3;

import service1.Time;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public class CargoThread implements Runnable {

    public final static int maxUnloadingMinutesDelay = 1440;
    public final static int arrivingDaysDeviationWindow = 14;

    private Time modelTime = new Time(1, 0, 0);
    private LinkedList<ScheduleElementKeeper> arrivingCargoList;
    private LinkedList<ScheduleElementKeeper> arrivedCargoList;
    private LinkedList<ScheduleElementKeeper> unloadedCargoList;
    private Crane[] arrayOfCranes;
    private int numberOfCranes;
    private int numberOfBusyCranes;
    private int queueLength;
    private int numberOfQueueEvents;
    private Comparator<ScheduleElementKeeper> comparator = (element1, element2)
            -> element1.getScheduleElement().getArrivingTime().
            compareTo(element2.getScheduleElement().getArrivingTime());

    public CargoThread(LinkedList<ScheduleElementKeeper> arrivingCargoList, int numberOfCranes)
    { // посмотреть, почему не разгружается груз
        this.arrivingCargoList = new LinkedList<ScheduleElementKeeper>(arrivingCargoList);
        arrivedCargoList = new LinkedList<ScheduleElementKeeper>();
        unloadedCargoList = new LinkedList<ScheduleElementKeeper>();
        arrayOfCranes = new Crane[numberOfCranes];

        for (int i = 0; i < arrayOfCranes.length; i++) {
            arrayOfCranes[i] = new Crane();
        }
        this.numberOfCranes = numberOfCranes;
        numberOfBusyCranes = 0;
        queueLength = 0;
        numberOfQueueEvents = 0;

        for (ScheduleElementKeeper scheduleElementKeeper:
             arrivingCargoList) {
            addTimeDeviations(scheduleElementKeeper);
        }

        Collections.sort(arrivingCargoList, comparator);

        while (!arrivingCargoList.isEmpty()) {
            if (arrivingCargoList.getFirst().getActualArrivingTime().getDay() < 1) {
                arrivingCargoList.removeFirst();
            } else {
                break;
            }
        }
    }

    public void run()
    {
        for (Crane crane: arrayOfCranes) {
            new Thread(crane).start();
        }

        getArrivingCargosIntoArrivedList();
        distributeCranesBetweenArrivedCargos();
        numberOfQueueEvents++;
        queueLength += countUnbusyShips();

        while (!arrivingCargoList.isEmpty() || !arrivedCargoList.isEmpty()) {
            double nearestEventInMinutes = getNearestEventInMinutes();
            modelTime.addMinutes(nearestEventInMinutes);

            if (modelTime.getDay() > 30) {
                break;
            }

            createChangeTimeEvent();
            getArrivingCargosIntoArrivedList();
            updateConditionOfFinishedCargos();
            distributeCranesBetweenArrivedCargos();
            numberOfQueueEvents++;
            queueLength += countUnbusyShips();
        }

        for (Crane crane: arrayOfCranes) {
            crane.disable();
        }
    }

    public LinkedList<ScheduleElementKeeper> getArrivedCargoList() {
        return arrivedCargoList;
    }

    public LinkedList<ScheduleElementKeeper> getUnloadedCargoList() { return unloadedCargoList; }

    public double getQueueLength() {
        return queueLength;
    }

    public int getNumberOfQueueEvents() {
        return numberOfQueueEvents;
    }

    private void addTimeDeviations(ScheduleElementKeeper cargo) {
        cargo.checkMinutesForUnloading();
        int unloadingMinutesDelay = (int)(Math.random() * maxUnloadingMinutesDelay);
        cargo.addMinutesForUnloading(unloadingMinutesDelay);

        Time deviatedArrivingTime = new Time(-(arrivingDaysDeviationWindow / 2) + 1,0 ,0);
        deviatedArrivingTime.addMinutes(Time.getRandomTime(arrivingDaysDeviationWindow).getTimeInMinutes()
                + cargo.getScheduleElement().getArrivingTime().getTimeInMinutes());
        cargo.setActualArrivingTime(deviatedArrivingTime);
    }

    private double getNearestEventInMinutes() {
        double minute = -1;
        for (ScheduleElementKeeper cargo: arrivedCargoList) {
            if (cargo.getNumberOfCranes() != 0) {
                if ((cargo.getMinutesForUnloading()
                        - cargo.getMinutesUnloaded())
                        / cargo.getNumberOfCranes()
                        < minute
                        || minute < 0) {
                    minute = (cargo.getMinutesForUnloading() - cargo.getMinutesUnloaded())
                            / cargo.getNumberOfCranes();
                }
            }
        }

        if (!arrivingCargoList.isEmpty()) {
            if (arrivingCargoList.getFirst().getActualArrivingTime().getTimeInMinutes()
                    - modelTime.getTimeInMinutes() < minute
                    || minute < 0) {
                minute = arrivingCargoList.getFirst().getActualArrivingTime().getTimeInMinutes()
                        - modelTime.getTimeInMinutes();
            }
        }

        return minute;
    }

    private void getArrivingCargosIntoArrivedList() {
        while (!arrivingCargoList.isEmpty()) {
            if (arrivingCargoList.getFirst().getActualArrivingTime().equals(modelTime)) {
                arrivedCargoList.add(arrivingCargoList.removeFirst());
            } else {
                break;
            }
        }
    }

    private void createChangeTimeEvent() {
        for (Crane crane: arrayOfCranes) {
            if (crane.isCraneBusy()) {
                crane.createChangeTimeEvent();
            }
        }
    }

    private void distributeCranesBetweenArrivedCargos() {
        if (!arrivedCargoList.isEmpty()) {
            while (numberOfCranes != numberOfBusyCranes) {
                boolean doesEveryCargoHaveTwoCranes = true;
                int indexOfTheMostIdlingCargo = 0;
                long minutesOfIdlingInPort = 0;

                for (int i = 0; i < arrivedCargoList.size(); i++) {
                    ScheduleElementKeeper cargo = arrivedCargoList.get(i);
                    int numberOfCargoCranes = cargo.getNumberOfCranes();
                    if (numberOfCargoCranes != 2) {
                        doesEveryCargoHaveTwoCranes = false;

                        long minutesOfAriving = Math.max(cargo.getScheduleElement()
                                        .getArrivingTime()
                                        .getTimeInMinutes(),
                                cargo.getActualArrivingTime()
                                        .getTimeInMinutes());
                        long minutesOfLocalCargoIdling = modelTime.getTimeInMinutes()
                                - cargo.getScheduleElement()
                                .getArrivingTime()
                                .getTimeInMinutes();

                        if (i == 0 || minutesOfLocalCargoIdling > minutesOfIdlingInPort) {
                            indexOfTheMostIdlingCargo = i;
                            minutesOfIdlingInPort = minutesOfLocalCargoIdling;
                        }
                    }
                }

                if (doesEveryCargoHaveTwoCranes) {
                    break;
                }

                try {
                    Crane crane = getUnbusyCraneForWork();
                    if (!crane.isActive()) {
                        throw new RuntimeException();
                    }

                    crane.getCargoForCrane(arrivedCargoList.get(indexOfTheMostIdlingCargo));
                    numberOfBusyCranes++;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Crane getUnbusyCraneForWork() {
        for (Crane crane: arrayOfCranes) {
            if (!crane.isCraneBusy()) {
                return crane;
            }
        }

        return new Crane();
    }

    private void updateConditionOfFinishedCargos() {
        if (!arrivedCargoList.isEmpty()) { //чекнуть, можно ли обойтись, тк вроде как можно
            int numberOfFinishedCranes = 0;
            ListIterator<ScheduleElementKeeper> iterator = arrivedCargoList.listIterator(0);

            while (iterator.hasNext()) {
                ScheduleElementKeeper cargo = iterator.next();
                if (cargo.isFinished()) {
                    numberOfFinishedCranes += cargo.getNumberOfCranes();
                    unloadedCargoList.add(cargo);
                    iterator.previous();
                    iterator.remove();
                }
            }

            numberOfBusyCranes -= numberOfFinishedCranes;
        }
    }

    private int countUnbusyShips() {
        int counter = 0;
        for (ScheduleElementKeeper cargo: arrivedCargoList) {
            if (cargo.getNumberOfCranes() == 0) {
                counter++;
            }
        }

        return counter;
    }

    private class Crane implements Runnable {

        private boolean isActive;
        private boolean isBusy;
        private boolean changeTimeEvent;
        private Time previousModelTime;
        private ScheduleElementKeeper cargo;

        public Crane() {
            isActive = false;
            isBusy = false;
            changeTimeEvent = false;
            previousModelTime = new Time(1,0,0);
        }

        public boolean isActive() {
            return isActive;
        }

        public boolean isCraneBusy() {
            try {
                if (!isActive) {
                    throw new RuntimeException();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            return isBusy;
        }

        public void disable() { isActive = false; }

        public void createChangeTimeEvent() {
            changeTimeEvent = true;
        }

        public void getCargoForCrane(ScheduleElementKeeper scheduleElementKeeper) {
            if (isActive) {
                isBusy = true;
                cargo = scheduleElementKeeper;
                synchronized (this) { cargo.addCrane(); }
                if (cargo.getNumberOfCranes() == 1) {
                    cargo.setStartUnloadingTime(modelTime);
                }
                previousModelTime = modelTime;
            }
        }

        public void run() {
            isActive = true;

            while (isActive) {
                if (changeTimeEvent) {
                    unloadCargo();
                    changeTimeEvent = false;
                }
            }
        }

        private synchronized void unloadCargo() {
            if (!cargo.isFinished()) {
                cargo.addMinutesUnloaded(modelTime.getTimeInMinutes() - previousModelTime.getTimeInMinutes());
                previousModelTime = modelTime;

                if (cargo.getMinutesUnloaded()
                        >= cargo.getMinutesForUnloading()) {
                    cargo.setFinishUnloadingTime(modelTime);
                    cargo.finish();
                    isBusy = false;
                }
            } else {
                isBusy = false;
            }
        }
    }
}

