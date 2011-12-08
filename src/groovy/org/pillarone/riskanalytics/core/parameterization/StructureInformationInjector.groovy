package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure
import org.pillarone.riskanalytics.core.util.ModelInjector
import org.pillarone.riskanalytics.core.util.GroovyUtils

class StructureInformationInjector extends ModelInjector {

    int periodCount = 1

    public StructureInformationInjector(ModelStructure structure, Model model) {
        super(structure)
        init(model)
    }

    public StructureInformationInjector(String configurationFileName, Model model) {
        super(configurationFileName)
        init(model)
    }

    public StructureInformationInjector(ModelStructureDAO modelStructure, Model model) {
        super(modelStructure.stringData.asConfigObject(), modelStructure.name)
        init(model)
    }


    private def init(Model model) {
        if (configObject.containsKey("periodCount")) {
            periodCount = configObject.periodCount
        }
        if (!configObject.isEmpty()) {
            configObject.company.each {line, value ->
                configObject.company[line].components.each {component, propertyValue ->
                    if (propertyValue.isEmpty()) {
                        configObject.company[line].components[component] = model[component]
                    } else {
                        injectConfig(propertyValue, model[component])
                    }
                }
            }
        }
    }

    void injectConfig(ConfigObject configObject, def component) {
        configObject.each {name, value ->
            if (value instanceof ConfigObject && !(value.isEmpty())) {
                injectConfig(value, component[name])
            } else {
                configObject[name] = component[name]
            }
        }


    }

    protected void injectConfigToModel(ConfigObject configObject, Model model) {
        injectConfigToModel configObject, model, 0
    }

    protected void injectConfigToModel(ConfigObject configObject, Model model, int period) {
        configObject.company.each {line, value ->
            configObject.company[line].each {propertyName, propertyValue ->
                if (propertyName != 'components') {
                    configObject.company[line].components.each {componentName, Component component ->
                        if (GroovyUtils.getProperties(component).containsKey(propertyName)) {
                            component[propertyName] = propertyValue[period]
                        }
                    }

                }
            }
        }
    }

    void injectConfiguration(Model model, int period) {
        checkModelMatch(configObject, model)
        injectConfigToModel(configObject, model, period)
    }

    protected ConfigObject loadConfiguration(String configurationFileName) {
        try {
            return super.loadConfiguration(configurationFileName)
        } catch (FileNotFoundException e) {
            return new ConfigObject()
        }
    }


}