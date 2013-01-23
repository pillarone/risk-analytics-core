package org.pillarone.riskanalytics.core.simulation;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class AfterSimulationEndException extends NotInProjectionHorizon {
    public AfterSimulationEndException(String message) {
        super(message);
    }
}
