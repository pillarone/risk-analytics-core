package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.mock.interceptor.StubFor
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.actions.IterationAction
import org.pillarone.riskanalytics.core.simulation.engine.actions.SimulationAction

class SimulationActionTests extends GroovyTestCase {

    void testProtocol() {
        int numberOfIterations = 2

        StubFor iterationActionStub = new StubFor(IterationAction)
        StubFor simulationScopeStub = new StubFor(SimulationScope)

        simulationScopeStub.demand.getNumberOfIterations {numberOfIterations}
        numberOfIterations.times {
            simulationScopeStub.demand.setCurrentIteration {}
            iterationActionStub.demand.perform {}
            simulationScopeStub.demand.getIterationsDone {println "getIterationsDone"; 0}
            simulationScopeStub.demand.setIterationsDone {}
        }

        iterationActionStub.use {
            simulationScopeStub.use {
                IterationAction action1 = new IterationAction()
                SimulationScope scope = new SimulationScope()
                SimulationAction action = new SimulationAction(iterationAction: action1, simulationScope: scope)
                action.perform()
            }
        }

    }
}