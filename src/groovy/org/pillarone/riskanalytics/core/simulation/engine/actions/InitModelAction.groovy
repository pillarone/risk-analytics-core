package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.ResourceRegistry
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

public class InitModelAction implements Action {

    private static Log LOG = LogFactory.getLog(InitModelAction)

    SimulationScope simulationScope

    public void perform() {
        LOG.debug "Initializing model"
        ResourceRegistry.clear()
        Model instance = simulationScope.model
        instance.init()
        instance.injectComponentNames()

        VersionNumber currentVersion = Model.getModelVersion(instance.class)
        VersionNumber parameterizationModelVersion = simulationScope.simulation.parameterization.modelVersionNumber
        if (currentVersion != parameterizationModelVersion) {
            throw new IllegalStateException("Deployed model has version ${currentVersion}, but parameterization has version ${parameterizationModelVersion}")
        }
    }


}