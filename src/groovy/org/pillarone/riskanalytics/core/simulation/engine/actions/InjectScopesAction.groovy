package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

public class InjectScopesAction implements Action {

    private static Log LOG = LogFactory.getLog(InjectScopesAction)

    SimulationScope simulationScope
    IterationScope iterationScope
    PeriodScope periodScope

    public void perform() {
        LOG.debug "Publishing scopes to components"
        simulationScope.model.accept(new ScopeInjector(simulationScope, iterationScope, periodScope))

        LOG.debug "Published scopes to components"
    }

}