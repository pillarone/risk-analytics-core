package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.actions.Action

public class InitModelAction implements Action {

    private static Log LOG = LogFactory.getLog(InitModelAction)

    SimulationScope simulationScope

    public void perform() {
        LOG.debug "Initializing model"
        Model instance = simulationScope.model
        instance.init()
        instance.injectComponentNames()
    }


}