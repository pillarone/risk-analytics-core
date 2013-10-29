package org.pillarone.riskanalytics.core.simulation.engine.actions

import models.core.CoreModelTests

import static org.junit.Assert.*


class PrepareParameterizationActionTests extends CoreModelTests {

    @Override
    void postSimulationEvaluation() {
        super.postSimulationEvaluation()
        assertSame runner.currentScope, runner.currentScope.model.exampleInputOutputComponent.injectedScope
        assertEquals 1, runner.currentScope.model.exampleInputOutputComponent.globalInt
        assertEquals "string", runner.currentScope.model.exampleInputOutputComponent.globalString

        assertSame runner.currentScope, runner.currentScope.model.exampleInputOutputComponent.parmParameterObject.injectedScope
        assertEquals 1, runner.currentScope.model.exampleInputOutputComponent.parmParameterObject.globalInt
        assertEquals "string", runner.currentScope.model.exampleInputOutputComponent.parmParameterObject.globalString
    }


}
