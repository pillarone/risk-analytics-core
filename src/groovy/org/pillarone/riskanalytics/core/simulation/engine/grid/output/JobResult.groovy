package org.pillarone.riskanalytics.core.simulation.engine.grid.output


class JobResult implements Serializable {

    String nodeName
    int totalMessagesSent
    int numberOfSimulatedPeriods = 1
    int completedIterations
    Throwable simulationException
    Date start
    Date end
}
