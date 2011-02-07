package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.components.InitializingComponent
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.parameterization.global.GlobalParameterTarget
import org.pillarone.riskanalytics.core.parameterization.global.GlobalParameterSource
import org.pillarone.riskanalytics.core.components.GlobalParameterComponent
import java.lang.reflect.Method

/**
 * Prepares the ParameterApplicator and applies the parameters of the first period. The later is required as following
 * actions like  {@code ApplyGlobalParametersAction} ,  {@code PrepareResourceParameterizationAction}  and
 * {@code WireModelAction}  depend on parameters.
 */
public class PrepareParameterizationAction implements Action {

    private static Log LOG = LogFactory.getLog(PrepareParameterizationAction)

    SimulationScope simulationScope
    PeriodScope periodScope

    private List<GlobalParameterTarget> globalTargets = []
    private List<GlobalParameterSource> globalSources = []
    private List<InitializingComponent> initializingComponents = []

    public void perform() {

        Model model = simulationScope.model
        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: simulationScope.parameters)
        applicator.init()
        simulationScope.parameterApplicator = applicator
        periodScope.parameterApplicator = applicator
        // PMO-758: Applying parameters before wiring is necessary,
        // similarly ApplyGlobalParameters and PrepareResourcesParameterizationAction depend on the following line
        simulationScope.parameterApplicator.applyParameterForPeriod(0)
        traverseModel(model)
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