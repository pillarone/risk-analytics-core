package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.actions.Action

public class PrepareParameterizationAction implements Action {

    private static Log LOG = LogFactory.getLog(PrepareParameterizationAction)

    SimulationScope simulationScope
    PeriodScope periodScope

    public void perform() {

        Model model = simulationScope.model
        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: simulationScope.parameters)
        applicator.init()
        simulationScope.parameterApplicator = applicator
        periodScope.parameterApplicator = applicator
    }


}