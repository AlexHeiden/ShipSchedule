package MainProgram;

import service1.Schedule;
import service1.ScheduleElement;
import service1.Time;
import service2.JSONService;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {

        Time mark = new Time("01:23:21");

        System.out.println(mark.getDay());

        System.out.println((int)(0.99));

        JSONService.getScheduleForModel(4, 3.2, 5.6);
        LinkedList<ScheduleElement> list = JSONService.getScheduleListFromJSON();
        Schedule.printSchedule(list);

    }
}
