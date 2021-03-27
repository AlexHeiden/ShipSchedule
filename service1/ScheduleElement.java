package service1;

public class ScheduleElement
{
    private Time arrivingTime;
    private String name;
    private CargoType cargoType;
    private double weight;
    private Time unloadingTime;

    public ScheduleElement(Time arrivingTime, String name, CargoType cargoType, double weight, Time unloadingTime)
    {
        this.arrivingTime = arrivingTime;
        this.name = name;
        this.cargoType = cargoType;
        this.weight = weight;
        this.unloadingTime = unloadingTime;
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
}
