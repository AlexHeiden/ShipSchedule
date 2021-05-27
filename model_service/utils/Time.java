package ru.stepanov.model_service.utils;

import java.security.InvalidParameterException;

public class Time implements Comparable<Time>
{
    private long day;
    private long hour;
    private long minute;

    public static final int maxDay = 30;
    public static final int maxHour = 23;
    public static final int maxMinute = 59;

    public Time(long day, long hour, long minute)
    {
        if ((hour < 0) || (hour > maxHour) || (minute < 0) || (minute > maxMinute))
        {
            throw new IllegalArgumentException();
        }

        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public Time(Time time) {
        day = time.day;
        hour = time.hour;
        minute = time.minute;
    }

    public Time(String string)
    {
        if (string.length() != 8)
        {
            throw new IllegalArgumentException();
        }


        String[] timeParameters = string.split(":");

        long tempDay = Long.parseLong(timeParameters[0]);
        day = tempDay;

        long tempHour = Long.parseLong(timeParameters[1]);

        if (tempHour < 0 || tempHour > maxHour) {
            throw new IllegalArgumentException();
        }

        hour = tempHour;

        long tempMinute = Long.parseLong(timeParameters[2]);

        if (tempMinute < 0 || tempMinute > maxMinute) {
            throw new IllegalArgumentException();
        }

        minute = tempMinute;
    }

    public void makeEqual(Time time) {
        day = time.getDay();
        hour = time.getHour();
        minute = time.getMinute();
    }

    public long getDay()
    {
        return day;
    }

    public long getHour()
    {
        return hour;
    }

    public long getMinute()
    {
        return minute;
    }

    @Override
    public int compareTo(Time time)
    {
        if (day < time.getDay())
        {
            return -1;
        }

        if (day > time.getDay())
        {
            return 1;
        }

        if (hour < time.getHour())
        {
            return -1;
        }

        if (hour > time.getHour())
        {
            return 1;
        }

        if (minute < time.getMinute())
        {
            return -1;
        }

        if (minute > time.getMinute())
        {
            return 1;
        }

        return  0;
    }

    public boolean equals(Time time) {
        return day == time.getDay()
                && hour == time.getHour()
                && minute == time.getMinute();
    }

    public void addMinutes(double minute)
    {
        try {
            if (minute < 0) {
                throw new InvalidParameterException();
            }
        }
        catch(InvalidParameterException e) {
            e.printStackTrace();
        }

        int minuteToAdd = (int)minute;
        this.minute += minuteToAdd;

        long hourToAdd = this.minute / 60;
        this.minute %= 60;
        this.hour += hourToAdd;

        long dayToAdd = this.hour / 24;
        this.hour %= 24;
        this.day += dayToAdd;
    }

    public long getTimeInMinutes() {
        return minute + 60 * (hour + 24 * day);
    }

    public Time getTimeOutOfMinutes(long minutes) {
        long tempMinute = minutes;
        long tempHour = minutes / (maxMinute + 1);
        tempMinute %= (maxMinute + 1);

        long tempDay = tempHour / (maxHour + 1);
        tempHour %= (maxHour + 1);

        return new Time(tempDay, tempHour, tempMinute);
    }

    public static Time getRandomTime(int numberOfDays)
    {
        int day = (int)(Math.random() * numberOfDays);
        int hour = (int)(Math.random() * (maxHour));
        int minute = (int)(Math.random() * (maxMinute));
        return new Time(day, hour, minute);
    }

    public static String toString(Time time) {
        StringBuilder stringBuilder = new StringBuilder();

        if (time.getDay() / 10 < 1) {
            stringBuilder.append("0");
        }
        stringBuilder.append(time.getDay() + ":");

        if (time.getHour() / 10 < 1) {
            stringBuilder.append("0");
        }
        stringBuilder.append(time.getHour() + ":");

        if (time.getMinute() / 10 < 1) {
            stringBuilder.append("0");
        }
        stringBuilder.append(time.getMinute());

        return stringBuilder.toString();
    }
}
