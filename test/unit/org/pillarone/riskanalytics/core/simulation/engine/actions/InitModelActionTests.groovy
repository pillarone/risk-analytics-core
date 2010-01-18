package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.mock.interceptor.StubFor
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class InitModelActionTests extends GroovyTestCase {

    void testPerform() {

        SimulationScope simulationScope = new SimulationScope()
        simulationScope.model = new EmptyModel()
        Action initAction = new InitModelAction(simulationScope: simulationScope)

        initAction.perform()
        // TODO (Sep 29, 2009, msh): find validation for init
        assertNotNull "model expected on simulationScope", simulationScope.model
        assertEquals "wrong model class", EmptyModel, simulationScope.model.class

    }

    void testProtocol() {
        StubFor modelStub = new StubFor(EmptyModel)
        StubFor scopeStub = new StubFor(SimulationScope)


        scopeStub.demand.getModel { new EmptyModel()}
        modelStub.demand.init {}
        modelStub.demand.injectComponentNames {}

        modelStub.use {
            scopeStub.use {
                SimulationScope simulationScope = new SimulationScope()
                Action initAction = new InitModelAction(simulationScope: simulationScope)
                initAction.perform()
            }
        }
    }
}