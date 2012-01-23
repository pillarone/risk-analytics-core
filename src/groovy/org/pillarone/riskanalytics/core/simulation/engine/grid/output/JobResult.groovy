package org.pillarone.riskanalytics.core.simulation.engine.grid.output


class JobResult implements Serializable {

    String nodeName
    int totalMessagesSent
    int numberOfSimulatedPeriods = 1
    Throwable simulationException
    Date start
    Date end
}
