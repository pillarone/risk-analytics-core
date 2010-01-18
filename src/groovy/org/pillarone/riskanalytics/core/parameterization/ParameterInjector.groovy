package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.util.ModelInjector

class ParameterInjector extends ModelInjector {

    int periodCount = 1

    public ParameterInjector(Parameterization parameretization) {
        super(parameretization)
        if (configObject.containsKey("periodCount")) {
            periodCount = configObject.periodCount
        }
    }

    public ParameterInjector(String configurationFileName) {
        super(configurationFileName)
        if (configObject.containsKey("periodCount")) {
            periodCount = configObject.periodCount
        }
    }

    public ParameterInjector(ConfigObject configObject) {
        super(configObject)
        if (configObject.containsKey("periodCount")) {
            periodCount = configObject.periodCount
        }
    }

    protected void injectConfigToModel(ConfigObject configObject, Model model) {
        injectConfigToModel configObject, model, 0
    }

    protected void injectConfigToModel(ConfigObject configObject, Model model, int period) {
        configObject.components.each {component, value ->
            injectConfig(value, model[component], period)
        }
    }

    private injectConfig(ConfigObject configObject, Object target, int period) {
        configObject.each {name, value ->
            if (value instanceof ConfigObject && value.values().any {it instanceof ConfigObject}) {
                injectConfig(value, target[name], period)
            }
            else {
                def valueToInject = value[period]
                injectSimulationModel(valueToInject)
                target[name] = valueToInject
            }
        }
    }

    private injectConfig(ConfigObject configObject, DynamicComposedComponent target, int period) {
        configObject.each {name, value ->
            if (value instanceof ConfigObject && value.values().any {it instanceof ConfigObject}) {
                Component subComponent = target.getComponentByName(name)
                if (subComponent == null) {
                    subComponent = target.createDefaultSubComponent()
                    subComponent.name = name
                    target.addSubComponent(subComponent)
                }
                injectConfig(value, subComponent, period)
            }
        }
    }

    private void injectSimulationModel(def valueToInject) {
    }

    private void injectSimulationModel(ConstrainedString valueToInject) {
        List<Component> allMarkedComponents = model.getMarkedComponents(valueToInject.markerClass)
        valueToInject.selectedComponent = allMarkedComponents.find {Component c -> c.name == valueToInject.stringValue }
    }

    private void injectSimulationModel(AbstractMultiDimensionalParameter valueToInject) {
        valueToInject.simulationModel = model
    }

    private void injectSimulationModel(IParameterObject valueToInject) {
        valueToInject.parameters.values().each {
            injectSimulationModel(it)
        }
    }

    void injectConfiguration(Model model, int period) {
        this.model = model
        checkModelMatch(configObject, model)
        injectConfigToModel(configObject, model, period)
    }
}
