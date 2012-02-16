package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.example.model.TestModelForInjection
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

class InjectScopesActionTests extends GroovyTestCase {

    void testInjectScopes() {

        PeriodScope periodScope = new PeriodScope()
        IterationScope iterationScope = new IterationScope(periodScope: periodScope)
        SimulationScope simulationScope = new SimulationScope(iterationScope: iterationScope)

        TestModelForInjection model = new TestModelForInjection()
        model.init()

        PacketCollector collector = new PacketCollector(path: "TestModelForInjection:componentCtx:output")
        collector.attachToModel(model, null)

        simulationScope.model = model

        InjectScopesAction action = new InjectScopesAction(
                simulationScope: simulationScope,
                iterationScope: iterationScope,
                periodScope: periodScope
        )

        action.perform()

        assertSame "periodScope not injected", periodScope, model.componentPS.periodScope
        assertSame "simulationScope not injected", simulationScope, model.componentCtx.simulationScope
        assertNull "No fallback simulationContext injected if simulationScope ist defined", model.componentCtx.simulationContext

        assertSame "No SimulationScope in collector injected", simulationScope, collector.simulationScope

        assertNotNull "No PeriodStore on component", model.componentCtxPS.periodStore
        assertTrue "PeriodStore not in IterationScope", iterationScope.periodStores.contains(model.componentCtxPS.periodStore)
    }
}