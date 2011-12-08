package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.util.ModelInjector
import org.pillarone.riskanalytics.core.util.GroovyUtils

class CollectorInjector extends ModelInjector {
    Set collectors

    public CollectorInjector(def simulationTemplate) {
        super(simulationTemplate)
        this.collectors = new HashSet()
    }

    public CollectorInjector(String configurationFileName) {
        super(configurationFileName)
        this.collectors = new HashSet()
    }

    protected void injectConfigToModel(ConfigObject configObject, Model model) {
        configObject.components.each {component, value ->
            injectConfig(value, model[component])
        }
    }

    protected void injectConfig(ConfigObject configObject, def object) {
        configObject.each {componentsPropertyName, value ->
            if (value instanceof ConfigObject) {
                // handle selection for all subComponents
                if (!GroovyUtils.getProperties(object).containsKey(componentsPropertyName) && object instanceof DynamicComposedComponent) {
                    object.allSubComponents().each {subComponent ->
                        injectConfig configObject[componentsPropertyName], subComponent
                    }
                } else {
                    injectConfig(value, object[componentsPropertyName])
                }
            } else if (value instanceof List) {
                value.each {
                    collectors << it
                    it.attachOutput(object, componentsPropertyName)
                }
            } else {
                collectors << value
                value.attachOutput(object, componentsPropertyName)
            }
        }
    }
}