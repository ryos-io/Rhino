package ${groupId}.benchmark;

import io.ryos.rhino.sdk.SimulationSpec;
import io.ryos.rhino.sdk.SimulationSpecImpl;

public class Rhino {

    private static final String PROPS = "classpath:///rhino.properties";
    private static final String SIM_NAME = "helloWorld";

    public static void main(String ... args) {
        SimulationSpec simulation = new SimulationSpecImpl(PROPS, SIM_NAME);
        simulation.start();
    }
}
