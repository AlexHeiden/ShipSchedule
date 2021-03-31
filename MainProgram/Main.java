package MainProgram;

import service2.JSONService;
import service3.ModelPreparer;

public class Main {

    public static void main(String[] args) {
        JSONService jsonService = new JSONService(100, 1, 5);
        jsonService.getScheduleForModel();
        ModelPreparer modelPreparer = new ModelPreparer();
    }
}
