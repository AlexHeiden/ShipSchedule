package service1;

public class Time implements Comparable<Time>
{
    private int day;
    private int hour;
    private int minute;

    public static final int maxDay = 30;
    public static final int maxHour = 23;
    public static final int maxMinute = 59;

    Time()
    {
        day = 0;
        hour = 0;
        minute = 0;
    }

    public Time(int day, int hour, int minute)
    {
        try
        {
            if ((day < 0) || (maxDay > 30) || (hour < 0) || (hour > maxHour) || (minute < 0) || (minute > 59))
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
            try {
                int temp = Integer.parseInt(timeParameters[0]);

                if (temp < 1 || temp > maxDay) {
                    throw new IllegalArgumentException();
                }

                day = temp;
            } catch (IllegalArgumentException e) {
                System.out.println("Day argument must be between 1 and 30 inclusive");
                System.exit(-1);
            }

            try {
                int temp = Integer.parseInt(timeParameters[1]);

                if (temp < 0 || temp > maxHour) {
                    throw new IllegalArgumentException();
                }

                hour = temp;
            } catch (IllegalArgumentException e) {
                System.out.println("Hour argument must be between 0 and 23 inclusive");
                System.exit(-1);
            }

            try {
                int temp = Integer.parseInt(timeParameters[2]);

                if (temp < 0 || temp > maxMinute) {
                    throw new IllegalArgumentException();
                }

                minute = temp;
            } catch (IllegalArgumentException e) {
                System.out.println("Minute argument must be between 0 and 59 inclusive");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.out.println("You must input time arguments this way: dd:hh:mm");
            System.exit(-1);
        }

    }

    public int getDay()
    {
        return day;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
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

    public void printTime()
    {
        System.out.println("" + day / 10 + day % 10 + ":" + hour / 10 + hour % 10 + ":" + minute / 10 + minute % 10);
    }

    public void addMinutes(double minute)
    {
        int minuteToAdd = (int)minute;
        this.minute += minuteToAdd;

        int hourToAdd = this.minute / 60;
        this.minute %= 60;
        this.hour += hourToAdd;

        int dayToAdd = this.hour / 24;
        this.hour %= 24;
        this.day += dayToAdd;
    }

    public static Time getRandomTime()
    {
        int day = (int)(Math.random() * (maxDay) + 1);
        int hour = (int)(Math.random() * (maxHour));
        int minute = (int)(Math.random() * (maxMinute));
        return new Time(day, hour, minute);
    }
}
