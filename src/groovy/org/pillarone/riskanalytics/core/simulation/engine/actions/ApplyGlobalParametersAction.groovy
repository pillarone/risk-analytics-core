package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.parameterization.global.GlobalParameterTarget
import org.pillarone.riskanalytics.core.parameterization.global.GlobalParameterSource
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.components.InitializingComponent
import org.pillarone.riskanalytics.core.components.GlobalParameterComponent
import java.lang.reflect.Method

class ApplyGlobalParametersAction implements Action {

    SimulationScope simulationScope

    private List<GlobalParameterTarget> globalTargets = []
    private List<GlobalParameterSource> globalSources = []
    private List<InitializingComponent> initializingComponents = []

    void perform() {
        traverseModel(simulationScope.model)
        applyGlobalParameters()
        initializingComponents*.afterParameterInjection(simulationScope)
    }

    private void applyGlobalParameters() {
        for (GlobalParameterTarget target in globalTargets) {
            String identifier = target.propertyName.substring(6).toLowerCase()
            GlobalParameterSource source = globalSources.find { it.identifier == identifier }
            if (source == null) {
                throw new IllegalStateException("No global parameter with name $identifier found.")
            }
            source.applyToTarget(target)
        }
    }

    private void traverseModel(Model model) {
        for (Component component in model.allComponents) {
            traverseModel(component)
        }
    }

    private void traverseModel(Component component) {
        for (Map.Entry<String, ?> prop in GroovyUtils.getProperties(component)) {
            if (prop.value instanceof Component || prop.value instanceof IParameterObject) {
                traverseModel(prop.value)
            }
            if (prop.key.startsWith("global")) {
                globalTargets << new GlobalParameterTarget(targetInstance: component, propertyName: prop.key)
            }
        }
        if (component instanceof InitializingComponent) {
            initializingComponents << component
        }
    }

    private void traverseModel(GlobalParameterComponent component) {
        for (Map.Entry<String, Method> entry in component.globalMethods) {
            globalSources << new GlobalParameterSource(identifier: entry.key, method: entry.value, source: component)
        }
    }

    private void traverseModel(IParameterObject parameterObject) {
        for (param in parameterObject.parameters) {
            if (param instanceof IParameterObject) {
                traverseModel(param)
            }
        }
        for (Map.Entry<String, ?> prop in GroovyUtils.getProperties(parameterObject)) {
            if (prop.key.startsWith("global")) {
                globalTargets << new GlobalParameterTarget(targetInstance: parameterObject, propertyName: prop.key)
            }
        }
        if (parameterObject instanceof InitializingComponent) {
            initializingComponents << parameterObject
        }
    }
}