package service3;

import service1.ScheduleElement;
import service1.Time;

public class ScheduleElementKeeper {

    private boolean isFinished;
    private ScheduleElement scheduleElement;
    private int numberOfCranes;
    private long minutesUnloaded;
    private long minutesForUnloading;
    private Time actualArrivingTime;
    private Time startUnloadingTime;
    private Time finishUnloadingTime;

    public ScheduleElementKeeper(ScheduleElement scheduleElement)
    {
        isFinished = false;
        this.scheduleElement = scheduleElement;
        numberOfCranes = 0;
        minutesUnloaded = 0;
        minutesForUnloading = 0;
        actualArrivingTime = new Time(0,0,0);
        startUnloadingTime = new Time(0,0,0);
        finishUnloadingTime = new Time(0,0,0);
    }

    public void finish() { isFinished = true; }
    public void addCrane() { numberOfCranes++; }
    public void addMinutesUnloaded(long minutes) { minutesUnloaded += minutes; }
    public void addMinutesForUnloading(long minutes) { minutesForUnloading += minutes; }
    public void checkMinutesForUnloading() {
        minutesForUnloading = scheduleElement
            .getUnloadingTime()
            .getTimeInMinutes();
    }
    public void setActualArrivingTime(Time time)
    {
        actualArrivingTime = time;
    }
    public void setStartUnloadingTime(Time time) { startUnloadingTime = time; }
    public void setFinishUnloadingTime(Time time) { finishUnloadingTime = time; }
    public ScheduleElement getScheduleElement() { return scheduleElement; }
    public int getNumberOfCranes() { return numberOfCranes; }
    public long getMinutesUnloaded() { return minutesUnloaded; }
    public long getMinutesForUnloading() { return minutesForUnloading; }
    public Time getActualArrivingTime() { return actualArrivingTime; }
    public Time getStartUnloadingTime() { return startUnloadingTime; }
    public Time getFinishUnloadingTime() { return finishUnloadingTime; }
    public boolean isFinished() { return isFinished; }

    public void print() {
        Time waitForStartUnloadingTime = new Time(0,0,0);
        long waitMinutes = startUnloadingTime.getTimeInMinutes()
                - actualArrivingTime.getTimeInMinutes();
        if (waitMinutes < 0) {
            waitMinutes = 0;
        }
        waitForStartUnloadingTime.addMinutes(waitMinutes);

        Time unloadingDuration = new Time(0,0,0);
        unloadingDuration.addMinutes(finishUnloadingTime.getTimeInMinutes()
                - startUnloadingTime.getTimeInMinutes());

        System.out.println(getScheduleElement().getName());
        System.out.println("Arriving time: ");
        actualArrivingTime.printTime();
        System.out.println("Waiting for start of unloading time: ");
        waitForStartUnloadingTime.printTime();
        System.out.println("Start unloading time: ");
        startUnloadingTime.printTime();
        System.out.println("Unloading duration: ");
        unloadingDuration.printTime();
        System.out.println();
    }
}
