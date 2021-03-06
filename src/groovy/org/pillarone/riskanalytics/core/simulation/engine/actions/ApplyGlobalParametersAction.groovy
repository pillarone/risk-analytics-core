package org.pillarone.riskanalytics.core.simulation.engine.actions

import groovy.transform.TypeChecked
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
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.output.PacketCollector

@TypeChecked
class ApplyGlobalParametersAction implements Action {

    SimulationScope simulationScope

    private List<GlobalParameterTarget> globalTargets = []
    private List<GlobalParameterSource> globalSources = []

    void perform() {
        traverseModel(simulationScope.model)
        applyGlobalParameters()
    }

    private void applyGlobalParameters() {
        for (GlobalParameterTarget target in globalTargets) {
            String identifier = target.propertyName.substring(6).toLowerCase()
            GlobalParameterSource source = globalSources.find { GlobalParameterSource it -> it.identifier == identifier }
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
        for (Map.Entry<String, Object> prop in GroovyUtils.getProperties(component).entrySet()) {
            if (prop.value instanceof Component) {
                traverseModel(prop.value as Component)
            } else if (prop.value instanceof IParameterObject) {
                traverseModel(prop.value as IParameterObject)
            }
            if (prop.key.startsWith("global")) {
                globalTargets << new GlobalParameterTarget(targetInstance: component, propertyName: prop.key)
            }
        }
        for (ITransmitter transmitter in component.allOutputTransmitter) {
            if (transmitter.receiver instanceof PacketCollector) {
                traverseModel(transmitter.receiver)
            }
        }
    }

    private void traverseModel(GlobalParameterComponent component) {
        for (Map.Entry<String, Method> entry in component.globalMethods.entrySet()) {
            globalSources << new GlobalParameterSource(identifier: entry.key, method: entry.value, source: component)
        }
    }

    private void traverseModel(IParameterObject parameterObject) {
        for (param in parameterObject.parameters) {
            if (param instanceof IParameterObject) {
                traverseModel(param as IParameterObject)
            }
        }
        for (Map.Entry<String, Object> prop in GroovyUtils.getProperties(parameterObject).entrySet()) {
            if (prop.key.startsWith("global")) {
                globalTargets << new GlobalParameterTarget(targetInstance: parameterObject, propertyName: prop.key)
            }
        }
    }
}