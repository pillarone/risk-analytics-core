package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

/**
 * A pre-simulation action reading resource parameters and adjusting the model accordingly (e.g. adding additional
 * sub components to dynamic composed components.
 * This action should be executed AFTER  {@code PrepareParameterizationAction} because the model may need its
 * parameters injected to read the correct resource parameters.
 */
@CompileStatic
class PrepareResourceParameterizationAction implements Action {

    SimulationScope simulationScope

    void perform() {
        simulationScope.model.injectResourceParameters()
    }
}