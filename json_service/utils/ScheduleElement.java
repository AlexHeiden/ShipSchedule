package ru.stepanov.json_service.utils;

import java.security.InvalidParameterException;

public class ScheduleElement
{
    private Time arrivingTime;
    private String name;
    private CargoType cargoType;
    private double weight;
    private Time unloadingTime;

    public ScheduleElement(Time arrivingTime, String name, CargoType cargoType, double weight)
    {
        try {
            if (arrivingTime.getHour() < 0 || arrivingTime.getHour() > Time.maxHour ||
                    arrivingTime.getMinute() < 0 || arrivingTime.getMinute() > Time.maxMinute) {
                throw new InvalidParameterException();
            }
        }
        catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        this.arrivingTime = arrivingTime;

        try {
            if (name.isEmpty()) {
                throw new InvalidParameterException();
            }
        }
        catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        this.name = name;

        this.cargoType = cargoType;

        try {
            if (weight <= 0) {
                throw new InvalidParameterException();
            }
        }
        catch (InvalidParameterException e)
        {
            e.printStackTrace();
        }
        this.weight = weight;

        unloadingTime = new Time(0, 0, 0);
        switch (cargoType)
        {
            case CONTAINER: {
                weight = (int)(weight + 1);
                unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / Schedule.containerUnitUnloadingPerHour));
                break;
            }
            case LOOSE: {
                unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / Schedule.looseUnitUnloadingPerHour));
                break;
            }
            default: {
                unloadingTime.addMinutes((Time.maxMinute + 1) * (weight / Schedule.liquidUnitUnloadingPerHour));
                break;
            }
        }
    }

    public ScheduleElement(ScheduleElement scheduleElement) {
        this.arrivingTime = new Time(scheduleElement.getArrivingTime());
        name = scheduleElement.getName();
        cargoType = scheduleElement.getCargoType();
        weight = scheduleElement.getWeight();
        unloadingTime = new Time(scheduleElement.getUnloadingTime());
    }

    public Time getArrivingTime()
    {
        return arrivingTime;
    }

    public String getName()
    {
        return name;
    }

    public CargoType getCargoType()
    {
        return cargoType;
    }

    public double getWeight()
    {
        return weight;
    }

    public Time getUnloadingTime() { return unloadingTime; }

    public void setUnloadingTime(Time unloadingTime) { this.unloadingTime = unloadingTime; }
}
