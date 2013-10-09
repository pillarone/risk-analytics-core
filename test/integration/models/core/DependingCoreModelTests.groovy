package models.core

import org.pillarone.riskanalytics.core.output.DBOutput
import org.pillarone.riskanalytics.core.output.ICollectorOutputStrategy
import org.pillarone.riskanalytics.core.output.NoOutput
import org.pillarone.riskanalytics.core.simulation.engine.DependingModelTest
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.wiring.WireCategory

import java.util.concurrent.CyclicBarrier


class DependingCoreModelTests  extends DependingModelTest {

    Class getModelClass() {
        CoreModel
    }

    @Override
    List<ParameterHolder> getRuntimeParameters() {
        return [
                ParameterHolderFactory.getHolder("runtimeInt", 0, 10) ,
                ParameterHolderFactory.getHolder("runtimeSanityChecks", 0, true) ,
        ]
    }

    void postSimulationEvaluation() {
        //same as coremodeltests; should apply for both models
        CoreModel model = runner.currentScope.model
        assertEquals(10, model.exampleInputOutputComponent.runtimeInt)
        assertTrue(model.exampleInputOutputComponent.doCalculationCalled)
        assertTrue(model.exampleInputOutputComponent.afterParameterInjectionCalled)

        model = runner2.currentScope.model
        assertEquals(10, model.exampleInputOutputComponent.runtimeInt)
        assertTrue(model.exampleInputOutputComponent.doCalculationCalled)
        assertTrue(model.exampleInputOutputComponent.afterParameterInjectionCalled)

    }

    @Override
    protected ICollectorOutputStrategy getOutputStrategy() {
        return new NoOutput()
    }

    @Override
    int getIterationCount() {
        return 10
    }

    @Override
    Closure createExtraWiringClosure(List<SimulationRunner> runners) {
        SimulationRunner r1 = runners[0]
        SimulationRunner r2 = runners[1]
        return {
            CyclicBarrier barrier = new CyclicBarrier(2)

            runner.simulationAction.iterationAction.periodAction.barrier = barrier
            runner2.simulationAction.iterationAction.periodAction.barrier = barrier

            CoreModel model1 = r1.currentScope.model
            CoreModel model2 = r2.currentScope.model
            WireCategory.doSetProperty(model2.exampleInputOutputComponent, "inValue",WireCategory.doGetProperty(model1.exampleOutputComponent, "outValue1"))
        }
    }
}
