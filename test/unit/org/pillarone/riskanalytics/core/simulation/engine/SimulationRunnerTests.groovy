package org.pillarone.riskanalytics.core.simulation.engine
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.actions.Action
import org.pillarone.riskanalytics.core.simulation.engine.actions.IterationAction
import org.pillarone.riskanalytics.core.simulation.engine.actions.PeriodAction
import org.pillarone.riskanalytics.core.simulation.engine.actions.SimulationAction
import org.pillarone.riskanalytics.core.simulation.item.Simulation

@TestMixin(GrailsUnitTestMixin)
class SimulationRunnerTests {

    void testSimulationRun() {

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope, numberOfPeriods: 2)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope, numberOfIterations: 2, model: new EmptyModel(), simulation: new Simulation("xy"), simulationBlocks: [])

        Action periodAction = new PeriodAction(periodScope: periodScope)
        Action iterationAction = new IterationAction(iterationScope: iterationScope, periodAction: periodAction)
        Action simulationAction = new SimulationAction(simulationScope: simulationScope, iterationAction: iterationAction)

        Action preSimulationAction = [perform: {
            LogFactory.getLog(Action).debug "performing preSimulationAction"
        }] as Action
        Action postSimulationAction = [perform: {
            LogFactory.getLog(Action).debug "performing postSimulationAction"
        }] as Action


        SimulationRunner runner = getSimulationRunner(simulationScope)
        runner.preSimulationActions << preSimulationAction
        runner.simulationAction = simulationAction
        runner.postSimulationActions << postSimulationAction

        runner.start()
    }

    void testSimulationRunStopping() {
        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope, numberOfPeriods: 2)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope, numberOfIterations: 10000, simulationBlocks: [], model: new EmptyModel())
        Action periodAction = new PeriodAction(periodScope: periodScope)
        Action iterationAction = new IterationAction(iterationScope: iterationScope, periodAction: periodAction)
        Action simulationAction = new SimulationAction(simulationScope: simulationScope, iterationAction: iterationAction)

        boolean postSimulationActionCalled = false
        Action preSimulationAction = [perform: {
            LogFactory.getLog(Action).debug "performing preSimulationAction"
        }] as Action
        Action postSimulationAction = [perform: { postSimulationActionCalled = true }] as Action

        SimulationRunner runner = getSimulationRunner(simulationScope)
        runner.preSimulationActions << preSimulationAction
        runner.simulationAction = simulationAction
        runner.postSimulationActions << postSimulationAction

        Thread.start {
            runner.start()
        }
        while (simulationScope.simulationState != SimulationState.FINISHED) {
            sleep(100)
        }
        assertTrue "postSimulationAction not performed", postSimulationActionCalled
        assertTrue "stopped too late", simulationScope.iterationScope.currentIteration < simulationScope.numberOfIterations - 1
    }

    void testCreateRunner() {

        SimulationRunner runner = SimulationRunner.createRunner()
        assertNotNull "no runner created", runner
        SimulationScope simulationScope = runner.simulationAction.simulationScope
        assertNotNull "No simulationScope on action", simulationScope
        assertNotNull "no iterationscope defined", simulationScope.iterationScope
        assertNotNull "no periodcope defined", simulationScope.iterationScope.periodScope
        assertNotNull "no simulationaction defined", runner.simulationAction
        assertNotNull "no iterationaction defined", runner.simulationAction.iterationAction
        assertNotNull "no periodaction defined", runner.simulationAction.iterationAction.periodAction

    }

    static SimulationRunner getSimulationRunner(SimulationScope simulationScope) {
        SimulationRunner runner = new SimulationRunner(currentScope: simulationScope)
        runner.metaClass.notifySimulationStateChanged = { Simulation simulation, SimulationState simulationState -> }
        return runner

    }
}