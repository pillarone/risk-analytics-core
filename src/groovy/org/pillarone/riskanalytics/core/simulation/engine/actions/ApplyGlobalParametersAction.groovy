package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

/**
 * A pre-simulation action modifying components according to 'global' parameters. Global in the sense that a specific
 * component contains parameters applied to several other components.  
 * This action should be executed AFTER  {@code PrepareParameterizationAction} because the model needs its
 * parameters injected to read the correct resource parameters.
 */
class ApplyGlobalParametersAction implements Action {

    SimulationScope simulationScope

    void perform() {
        simulationScope.model.applyGlobalParameters()
    }
}