package org.pillarone.riskanalytics.core.simulation

public enum SimulationState {
    NOT_RUNNING,
    INITIALIZING,
    RUNNING,
    SAVING_RESULTS,
    POST_SIMULATION_CALCULATIONS,
    FINISHED,
    CANCELED,
    ERROR
}