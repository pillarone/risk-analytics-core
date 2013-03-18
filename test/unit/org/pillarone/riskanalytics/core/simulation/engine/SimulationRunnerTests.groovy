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
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope, numberOfIterations: 2, model: new EmptyModel())

        Action periodAction = new PeriodAction(periodScope: periodScope)
        Action iterationAction = new IterationAction(iterationScope: iterationScope, periodAction: periodAction)
        Action simulationAction = new SimulationAction(simulationScope: simulationScope, iterationAction: iterationAction)

        Action preSimulationAction = [perform: { LogFactory.getLog(Action).debug "performing preSimulationAction" }] as Action
        Action postSimulationAction = [perform: { LogFactory.getLog(Action).debug "performing postSimulationAction" }] as Action


        SimulationRunner runner = getSimulationRunner(simulationScope)
        runner.preSimulationActions << preSimulationAction
        runner.simulationAction = simulationAction
        runner.postSimulationActions << postSimulationAction

        runner.start()
    }

    void testSimulationRunStopping() {
        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope, numberOfPeriods: 2)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope, numberOfIterations: 10000)

        Action periodAction = new PeriodAction(periodScope: periodScope)
        Action iterationAction = new IterationAction(iterationScope: iterationScope, periodAction: periodAction)
        Action simulationAction = new SimulationAction(simulationScope: simulationScope, iterationAction: iterationAction)

        boolean postSimulationActionCalled = false
        Action preSimulationAction = [perform: { LogFactory.getLog(Action).debug "performing preSimulationAction" }] as Action
        Action postSimulationAction = [perform: { postSimulationActionCalled = true }] as Action

        SimulationRunner runner = getSimulationRunner(simulationScope)
        runner.preSimulationActions << preSimulationAction
        runner.simulationAction = simulationAction
        runner.postSimulationActions << postSimulationAction

        Thread.start {
            runner.start()
            assertTrue "stopped too late", simulationScope.currentIteration < simulationScope.numberOfIterations - 1
            assertTrue "postSimulationAction not performed", postSimulationActionCalled

        }
        Thread.sleep 2000
        runner.cancel()
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

    SimulationRunner getSimulationRunner(SimulationScope simulationScope) {
        SimulationRunner runner = new SimulationRunner(currentScope: simulationScope)
        runner.metaClass.notifySimulationStateChanged = { Simulation simulation, SimulationState simulationState -> }
        return runner

    }
    //TODO create similar test for grid configuration
    /*void testErrorDuringSimulation() {
        //to simulate an error we throw an exception when the withTransaction closure is used. (during simulation)
        transactionStub.demand.withTransaction(3..3) {
            Closure c ->
            //don't throw an exception when withTransaction is used from to load/delete data
            if (c.delegate instanceof ModellingItem) return
            throw new Exception()
        }
        deletionServiceStub.demand.getInstance(1..1) {
            return [
                    deleteSimulation: {SimulationRun run ->
                        //simulating marked to delete
                        run.parameterization = null
                        run.resultConfiguration = null
                    }
            ] as DeleteSimulationService
        }

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope, numberOfPeriods: 2)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope, numberOfIterations: 10, simulation: new Simulation("simulation"), model: new EmptyModel())

        PeriodAction periodAction = [perform: {throw new Exception()}] as PeriodAction
        Action iterationAction = new IterationAction(iterationScope: iterationScope, periodAction: periodAction)
        Action simulationAction = new SimulationAction(simulationScope: simulationScope, iterationAction: iterationAction)

        volatile boolean postSimulationActionCalled = false
        Action preSimulationAction = [perform: {LogFactory.getLog(Action).debug "performing preSimulationAction"}] as Action
        Action postSimulationAction = [perform: {postSimulationActionCalled = true}] as Action

        SimulationRunner runner = new SimulationRunner(currentScope: simulationScope)
        runner.preSimulationActions << preSimulationAction
        runner.simulationAction = simulationAction
        runner.postSimulationActions << postSimulationAction

        transactionStub.use {
            deletionServiceStub.use {
                runner.start()
            }
        }

        assertSame "simulation state after error", SimulationState.ERROR, runner.simulationState
        assertNotNull "error object not set", runner.error

        assertNull simulationScope.simulation.simulationRun?.parameterization
        assertNull simulationScope.simulation.simulationRun?.resultConfiguration
    }*/

    // TODO (Oct 21, 2009, msh): maybe dk has an idea why this test doesn't work
/*
    void testScopeConfiguration() {

        mockDomain ParameterizationDAO
        mockDomain ResultConfiguration
        mockDomain SimulationRun

        ParameterizationDAO params = new ParameterizationDAO(periodCount: 1, name:"params", modelClassName:"model", itemVersion:"1")
        assertNotNull params.save()
        ResultConfiguration resultConfig = new ResultConfiguration(name: "result", collectorInformation: [])
        assertNotNull resultConfig.save()
        println params.dump()
        println resultConfig.dump()
        SimulationRun simulationRun = new SimulationRun()
        simulationRun.name = "run"
        simulationRun.parameterization = params
        simulationRun.resultConfiguration = resultConfig
        simulationRun.model = CapitalEagleModel.name
        simulationRun.iterations = 10
        simulationRun.periodCount = 1
        println simulationRun.dump()
        assertNotNull simulationRun.save()

        FileOutput outputStrategy = new FileOutput()
        SimulationConfiguration configuration = new SimulationConfiguration(simulationRun: simulationRun, outputStrategy: outputStrategy)

        SimulationRunner runner = SimulationRunner.createRunner()
        assertNotNull "no runner created", runner

        runner.simulationConfiguration = configuration

        SimulationScope simulationScope = runner.simulationAction.simulationScope

        assertEquals "iterationCount", simulationRun.iterations, simulationScope.numberOfIterations
        assertEquals "model class", simulationRun.model, simulationScope.model.class.name
        assertSame "wrong simulationRun", simulationRun, simulationScope.simulationRun
        assertSame "wrong parameterization", simulationRun.parameterization, simulationScope.parameters
        assertSame "wrong result config", simulationRun.resultConfiguration, simulationScope.resultConfiguration
        assertSame "wrong output strategy", outputStrategy, simulationScope.outputStrategy

    }
*/


}