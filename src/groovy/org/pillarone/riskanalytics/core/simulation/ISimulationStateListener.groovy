package org.pillarone.riskanalytics.core.simulation

import org.pillarone.riskanalytics.core.simulation.SimulationState

public interface ISimulationStateListener {

    void simulationStateChanged(SimulationState oldState, SimulationState newState)

}