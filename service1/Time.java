package service1;

import java.security.InvalidParameterException;

public class Time implements Comparable<Time>
{
    private long day;
    private long hour;
    private long minute;

    public static final int maxDay = 30;
    public static final int maxHour = 23;
    public static final int maxMinute = 59;

    Time()
    {
        day = 0;
        hour = 0;
        minute = 0;
    }

    public Time(long day, long hour, long minute)
    {
        try
        {
            if ((hour < 0) || (hour > maxHour) || (minute < 0) || (minute > maxMinute))
            {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("You should input appropriate time");
            System.exit(-1);
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
        try
        {
            if (string.length() != 8)
            {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Time must contain 8 symbols");
            System.exit(-1);
        }

        String[] timeParameters = string.split(":");

        try {
                long tempDay = Long.parseLong(timeParameters[0]);
                day = tempDay;

            try {
                long tempHour = Long.parseLong(timeParameters[1]);

                if (tempHour < 0 || tempHour > maxHour) {
                    throw new IllegalArgumentException();
                }

                hour = tempHour;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            try {
                long tempMinute = Long.parseLong(timeParameters[2]);

                if (tempMinute < 0 || tempMinute > maxMinute) {
                    throw new IllegalArgumentException();
                }

                minute = tempMinute;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    public void printTime()
    {
        System.out.println("" + day / 10 + day % 10 + ":" + hour / 10 + hour % 10 + ":" + minute / 10 + minute % 10);
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

    public void addTime(Time time)
    {
        try {
            if (time.day < 0 || time.hour < 0 || time.minute < 0) {
                throw new InvalidParameterException();
            }
        }
        catch (InvalidParameterException e) {
            e.printStackTrace();
        }

        addMinutes(time.getTimeInMinutes());
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
}
