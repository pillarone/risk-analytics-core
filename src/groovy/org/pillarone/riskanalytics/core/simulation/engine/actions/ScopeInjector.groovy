package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.components.IResource
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.components.IterationStore
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.output.PacketCollector


class ScopeInjector implements IModelVisitor {

    public static final String SIMULATION_SCOPE_PROPERTY = "simulationScope"
    public static final String ITERATION_SCOPE_PROPERTY = "iterationScope"
    public static final String PERIOD_SCOPE_PROPERTY = "periodScope"

    SimulationScope simulationScope
    IterationScope iterationScope
    PeriodScope periodScope

    ScopeInjector(SimulationScope simulationScope, IterationScope iterationScope, PeriodScope periodScope) {
        this.simulationScope = simulationScope
        this.iterationScope = iterationScope
        this.periodScope = periodScope
    }

    void visitComponent(Component component, ModelPath path) {
        injectScopes(component)
        createStoreForComponentIfNeeded(component)

        component.allOutputTransmitter.each {ITransmitter transmitter ->
            if (transmitter.receiver instanceof PacketCollector) {
                injectScopes(transmitter.receiver)
            }
        }
        component.idGenerator = simulationScope.idGenerator
    }

    void visitModel(Model model) {

    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {

    }

    void visitResource(IResource resource, ModelPath path) {
        injectScopes(resource)
        createStoreForComponentIfNeeded(resource)
    }

    private void injectScopes(def object) {
        if (GroovyUtils.getProperties(object).keySet().contains(SIMULATION_SCOPE_PROPERTY)) {
            object[SIMULATION_SCOPE_PROPERTY] = simulationScope
        }
        if (GroovyUtils.getProperties(object).keySet().contains(ITERATION_SCOPE_PROPERTY)) {
            object[ITERATION_SCOPE_PROPERTY] = iterationScope
        }
        if (GroovyUtils.getProperties(object).keySet().contains(PERIOD_SCOPE_PROPERTY)) {
            object[PERIOD_SCOPE_PROPERTY] = periodScope
        }
    }

    /**
     * Injects a PeriodStore or IterationStore if a component contains
     * a corresponding property. The store is added to the IterationScope.
     * A component may have both kinds of stores.
     * @param component
     */
    private void createStoreForComponentIfNeeded(def component) {
        Set<String> propertyNames = GroovyUtils.getProperties(component).keySet()
        if (propertyNames.contains('periodStore')) {
            component.periodStore = new PeriodStore(periodScope)
            iterationScope.periodStores << component.periodStore
        }
        if (propertyNames.contains('iterationStore')) {
            component.iterationStore = new IterationStore(iterationScope)
        }
    }


}
