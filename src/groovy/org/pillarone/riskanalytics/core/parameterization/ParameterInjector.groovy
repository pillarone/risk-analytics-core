package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.util.ModelInjector
/**
 * This was the parameter injection mechanism of the old simulation engine.
 * It it is still used to import parameterizations in config object format
 * by applying the values of the config object to the model and then creating a
 * default parameterization.
 *
 * As a side effect sub components are added to dynamic components, if they do not exist
 * yet. The simulation model is no longer injected into components, therefore the class is not suitable for simulations.
 */
@Deprecated class ParameterInjector extends ModelInjector {

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
            } else {
                throw new IllegalArgumentException("$name does not contain any parameters.")
            }
        }
    }

    void injectConfiguration(Model model, int period) {
        this.model = model
        checkModelMatch(configObject, model)
        injectConfigToModel(configObject, model, period)
    }
}
