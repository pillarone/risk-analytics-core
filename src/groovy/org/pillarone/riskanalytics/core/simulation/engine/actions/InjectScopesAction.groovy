package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.PeriodStore
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.core.components.IterationStore

public class InjectScopesAction implements Action {

    private static Log LOG = LogFactory.getLog(InjectScopesAction)

    SimulationScope simulationScope
    IterationScope iterationScope
    PeriodScope periodScope

    public void perform() {
        LOG.debug "Publishing scopes to components"
        WiringUtils.forAllComponents(simulationScope.model) {originName, Component component ->
            addFallBackSimulationContext(component)
            injectScope "simulationScope", component
            injectScope "iterationScope", component
            injectScope "periodScope", component

            component.allOutputTransmitter.each {ITransmitter transmitter ->
                if (transmitter.receiver instanceof PacketCollector) {
                    injectScope "simulationScope", transmitter.receiver
                    injectScope "iterationScope", transmitter.receiver
                    injectScope "periodScope", transmitter.receiver
                }
            }

            createStoreForComponentIfNeeded(component)

        }

        LOG.debug "Published scopes to components"
    }

    /**
     * Injects a PeriodStore or IterationStore if a component contains
     * a corresponding property. The store is added to the IterationScope.
     * A component may have both kinds of stores.
     * @param component
     */
    private void createStoreForComponentIfNeeded(Component component) {
        if (component.properties.keySet().contains('periodStore')) {
            component.periodStore = new PeriodStore(periodScope)
            iterationScope.periodStores << component.periodStore
        }
        if (component.properties.keySet().contains('iterationStore')) {
            component.iterationStore = new IterationStore(iterationScope)
        }
    }

    private void injectScope(String scopeName, Component component) {
        if (component.properties.keySet().contains(scopeName)) {
            component[scopeName] = this[scopeName]
        }
    }

    private void addFallBackSimulationContext(Component component) {
        if (component.properties.keySet().contains("simulationContext") && !component.properties.keySet().contains("simulationScope")) {
            component["simulationContext"] = simulationScope
        }

    }


}