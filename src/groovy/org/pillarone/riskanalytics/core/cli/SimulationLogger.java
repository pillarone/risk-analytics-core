package org.pillarone.riskanalytics.core.cli;

import org.apache.commons.logging.Log;
import org.pillarone.riskanalytics.core.simulation.SimulationState;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner;

import java.util.ArrayList;
import java.util.List;

public class SimulationLogger implements Runnable {

    private final SimulationRunner simulationRunner;
    private final Log simulationLog;
    private static final long interval = 1000;

    private final List<SimulationState> stoppedStates = new ArrayList<SimulationState>(3);

    public SimulationLogger(SimulationRunner simulationRunner, Log simulationLog) {
        this.simulationRunner = simulationRunner;
        this.simulationLog = simulationLog;

        stoppedStates.add(SimulationState.FINISHED);
        stoppedStates.add(SimulationState.ERROR);

        new Thread(this).start();
    }

    public void run() {
        while (!stoppedStates.contains(simulationRunner.getSimulationState())) {
            if (simulationRunner.getSimulationState() == SimulationState.RUNNING) {
                simulationLog.info("Current iteration: " + simulationRunner.getCurrentScope().getIterationsDone());
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                simulationLog.error(e.getMessage(), e);
            }
        }
        synchronized (this) {
            notify();
        }
    }


}
