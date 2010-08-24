package org.pillarone.riskanalytics.core.simulation.engine.grid.output


class JobResult implements Serializable {

    String nodeName
    int totalMessagesSent
    Throwable simulationException
    Date start
    Date end
}
