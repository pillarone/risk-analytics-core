package org.pillarone.riskanalytics.core.simulation.engine.actions

import models.core.CoreModelTests


class PrepareParameterizationActionTests extends CoreModelTests {

    @Override
    void postSimulationEvaluation() {
        super.postSimulationEvaluation()
        assertSame runner.currentScope, runner.currentScope.model.exampleInputOutputComponent.injectedScope
        assertSame runner.currentScope, runner.currentScope.model.exampleInputOutputComponent.parmParameterObject.injectedScope
    }


}
