package ${groupId}.benchmark;

import io.ryos.rhino.sdk.SimulationSpec;
import io.ryos.rhino.sdk.SimulationSpecImpl;
import io.ryos.rhino.sdk.utils.Environment;

public class Rhino {
    public static void main(String ... args) {
        SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties",
            Environment.STAGE, "Server-Status Simulation Without User");
        simulation.start();
    }
}
