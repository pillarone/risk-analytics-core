package org.pillarone.riskanalytics.core.components

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope


public interface InitializingComponent {

    void afterParameterInjection(SimulationScope scope)

}