package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic


/**
 * An Exception occured during a simulation is encapsulated in this SimulationError.
 *
 * The SimulationError provides information about which kind of Exception occured when.
 */
@CompileStatic
public class SimulationError {

    def simulationRunID
    int iteration
    int period
    Throwable error
}