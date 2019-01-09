package ${groupId}.benchmark;

import com.adobe.rhino.sdk.SimulationSpec;
import com.adobe.rhino.sdk.SimulationSpecImpl;
import com.adobe.rhino.sdk.utils.Environment;

public class Rhino {
    public static void main(String ... args) {
        SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties",
            Environment.STAGE, "Server-Status Simulation Without User");
        simulation.start();
    }
}
