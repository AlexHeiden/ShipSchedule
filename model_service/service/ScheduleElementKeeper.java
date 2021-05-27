package ru.stepanov.model_service.service;

import ru.stepanov.model_service.utils.ScheduleElement;
import ru.stepanov.model_service.utils.Time;

public class ScheduleElementKeeper {

    private boolean isFinished;
    private ScheduleElement scheduleElement;
    private int numberOfCranes;
    private long minutesUnloaded;
    private long minutesForUnloading;
    private Time actualArrivingTime;
    private Time startUnloadingTime;
    private Time finishUnloadingTime;

    public ScheduleElementKeeper(ScheduleElement scheduleElement) {
        isFinished = false;
        this.scheduleElement = scheduleElement;
        numberOfCranes = 0;
        minutesUnloaded = 0;
        minutesForUnloading = 0;
        actualArrivingTime = new Time(0,0,0);
        startUnloadingTime = new Time(0,0,0);
        finishUnloadingTime = new Time(0,0,0);
    }

    public ScheduleElementKeeper(ScheduleElementKeeper cargo) {
        this.isFinished = cargo.isFinished;
        this.scheduleElement = new ScheduleElement(cargo.getScheduleElement());
        this.numberOfCranes = cargo.numberOfCranes;
        this.minutesUnloaded = cargo.minutesUnloaded;
        this.minutesForUnloading = cargo.minutesForUnloading;
        this.actualArrivingTime = new Time(cargo.actualArrivingTime);
        this.startUnloadingTime = new Time(cargo.startUnloadingTime);
        this.finishUnloadingTime = new Time(cargo.finishUnloadingTime);
    }

    public void finish() { isFinished = true; }
    public void addCrane() { numberOfCranes++; }
    public void addMinutesUnloaded(long minutes) { minutesUnloaded += minutes; }
    public void addMinutesForUnloading(long minutes) { minutesForUnloading += minutes; }
    public void setActualArrivingTime(Time time)
    {
        actualArrivingTime = new Time(time);
    }
    public void setStartUnloadingTime(Time time) { startUnloadingTime = new Time(time); }
    public void setFinishUnloadingTime(Time time) { finishUnloadingTime = new Time(time); }
    public ScheduleElement getScheduleElement() { return scheduleElement; }
    public int getNumberOfCranes() { return numberOfCranes; }
    public String getName() { return scheduleElement.getName(); }
    public long getMinutesUnloaded() { return minutesUnloaded; }
    public long getMinutesForUnloading() { return minutesForUnloading; }
    public Time getActualArrivingTime() { return actualArrivingTime; }
    public Time getStartUnloadingTime() { return startUnloadingTime; }
    public Time getFinishUnloadingTime() { return finishUnloadingTime; }
    public boolean isFinished() { return isFinished; }

    public void checkMinutesForUnloading() {
        minutesForUnloading = scheduleElement
                .getUnloadingTime()
                .getTimeInMinutes();
    }

    public Time getWaitForStartOfUnloadingTime() {
        Time waitForStartUnloadingTime = new Time(0,0,0);
        long waitMinutes = startUnloadingTime.getTimeInMinutes()
                - actualArrivingTime.getTimeInMinutes();
        if (waitMinutes < 0) {
            waitMinutes = 0;
        }
        waitForStartUnloadingTime.addMinutes(waitMinutes);

        return waitForStartUnloadingTime;
    }

    public Time getUnloadingDuration() {
        Time unloadingDuration = new Time(0,0,0);
        unloadingDuration.addMinutes(finishUnloadingTime.getTimeInMinutes()
                - startUnloadingTime.getTimeInMinutes());

        return unloadingDuration;
    }
}
