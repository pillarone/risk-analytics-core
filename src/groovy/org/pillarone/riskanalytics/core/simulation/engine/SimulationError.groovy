package org.pillarone.riskanalytics.core.simulation.engine


/**
 * An Exception occured during a simulation is encapsulated in this SimulationError.
 *
 * The SimulationError provides information about which kind of Exception occured when.
 */
public class SimulationError {

    def simulationRunID
    def iteration
    def period
    def error
}