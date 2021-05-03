package MainProgram;

import service1.CargoType;
import service1.ScheduleElement;
import service1.Time;
import service2.JSONService;
import service3.ModelPreparer;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static JSONService jsonService;

    public static void main(String[] args) {
        jsonService = new JSONService(100, 1, 5);
        jsonService.createNewShips();
        jsonService.getScheduleForModel();
        ModelPreparer modelPreparer = new ModelPreparer();
    }


}
