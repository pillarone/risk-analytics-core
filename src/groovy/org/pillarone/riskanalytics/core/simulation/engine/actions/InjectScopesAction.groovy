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
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.output.FileOutput

public class InjectScopesAction implements Action {

    private static Log LOG = LogFactory.getLog(InjectScopesAction)

    SimulationScope simulationScope
    IterationScope iterationScope
    PeriodScope periodScope

    public void perform() {
        LOG.debug "Publishing scopes to components"
        for (Component component : simulationScope.model.allComponentsRecursively) {
            addFallBackSimulationContext(component)
            injectScope "simulationScope", component
            injectScope "iterationScope", component
            injectScope "periodScope", component

            component.allOutputTransmitter.each {ITransmitter transmitter ->
                if (transmitter.receiver instanceof PacketCollector) {
                    injectScope "simulationScope", transmitter.receiver
                    injectScope "iterationScope", transmitter.receiver
                    injectScope "periodScope", transmitter.receiver

                    if (transmitter.receiver.outputStrategy instanceof FileOutput) {
                        injectScope "simulationScope", transmitter.receiver.outputStrategy
                    }
                }
            }

            createStoreForComponentIfNeeded(component)
            component.idGenerator = simulationScope.idGenerator

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
        Set<String> propertyNames = GroovyUtils.getProperties(component).keySet()
        if (propertyNames.contains('periodStore')) {
            component.periodStore = new PeriodStore(periodScope)
            iterationScope.periodStores << component.periodStore
        }
        if (propertyNames.contains('iterationStore')) {
            component.iterationStore = new IterationStore(iterationScope)
        }
    }

    private void injectScope(String scopeName, def component) {
        if (GroovyUtils.getProperties(component).keySet().contains(scopeName)) {
            component[scopeName] = this[scopeName]
        }
    }

    private void addFallBackSimulationContext(Component component) {
        Set<String> propertyNames = GroovyUtils.getProperties(component).keySet()
        if (propertyNames.contains("simulationContext") && !propertyNames.contains("simulationScope")) {
            component["simulationContext"] = simulationScope
        }

    }


}