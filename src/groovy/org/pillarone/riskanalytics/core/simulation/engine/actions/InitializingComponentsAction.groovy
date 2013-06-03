package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.components.IResource
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.components.InitializingComponent


class InitializingComponentsAction implements Action {

    SimulationScope simulationScope

    void perform() {
        simulationScope.model.accept(new IModelVisitor() {
            void visitModel(Model model) {
                process(model)
            }

            void visitComponent(Component component, ModelPath path) {
                process(component)
            }

            void visitResource(IResource resource, ModelPath path) {
                process(resource)
            }

            void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
                process(parameterObject)
            }

            protected void process(def value) {
            }

            protected void process(InitializingComponent component) {
                component.afterParameterInjection(simulationScope)
            }
        })
    }
}
