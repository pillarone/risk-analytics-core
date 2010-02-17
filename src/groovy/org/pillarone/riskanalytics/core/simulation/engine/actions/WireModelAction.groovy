package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.CollectorFactory
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

public class WireModelAction implements Action {

    private static Log LOG = LogFactory.getLog(WireModelAction)

    SimulationScope simulationScope

    public void perform() {
        LOG.debug "Wiring model"

        Model model = simulationScope.model
        ResultConfigurationDAO resultConfig = simulationScope.resultConfiguration
        // TODO (Oct 27, 2009, msh): Really required here ? ParameterApplicator creates subComponents for DCC already
        //TODO (msp 24.12.09): I don't think so..  Parameters for the 1st period are also injected twice this way
//        simulationScope.parameterApplicator.applyParameterForPeriod(0)

        model.wire()
        LOG.debug "Model wired"


        CollectorFactory collectorFactory = simulationScope.collectorFactory
        collectorFactory.structureInformation = simulationScope.structureInformation

        //PMO-654: make sure that the collectors are always wired in the same order to enable robust model reference result comparison
        List collectors = collectorFactory.createCollectors(resultConfig, model).sort { it.path }
        collectors.each {PacketCollector it ->
            it.attachToModel(model, simulationScope.structureInformation)
        }

        LOG.debug "Collectors attached"

        LOG.debug "Optimizing wiring"
        model.optimizeComposedComponentWiring()
    }


}
