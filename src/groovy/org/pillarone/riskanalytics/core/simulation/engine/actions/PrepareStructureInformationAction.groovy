package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.StructureInformation
import org.pillarone.riskanalytics.core.parameterization.StructureInformationInjector
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

public class PrepareStructureInformationAction implements Action {

    private static Log LOG = LogFactory.getLog(PrepareStructureInformationAction)


    SimulationScope simulationScope

    public void perform() {
        LOG.debug "Preparing StructureInformation"
        Model model = simulationScope.model
        StructureInformationInjector structureInformationInjector = new StructureInformationInjector(simulationScope.simulation.structure, model)
        simulationScope.structureInformation = new StructureInformation(structureInformationInjector.configObject, model)
        LOG.debug "StructureInformation published to SimulationScope"
    }


}