package ${groupId}.benchmark;

import io.ryos.rhino.sdk.SimulationSpec;
import io.ryos.rhino.sdk.SimulationSpecImpl;

public class Rhino {
    public static void main(String ... args) {
        SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties",
            "Server-Status Simulation Without User");
        simulation.start();
    }
}
