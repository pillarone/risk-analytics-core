package org.pillarone.riskanalytics.core.simulation.engine.actions

import org.pillarone.riskanalytics.core.model.IModelVisitor
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.ModelPath
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.parameterization.ApplicableParameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.IResource


class RuntimeParameterCollector implements IModelVisitor {

    public static final String RUNTIME_PARAMETER_PREFIX = "runtime"

    private static final Log LOG = LogFactory.getLog(RuntimeParameterCollector)

    List<ApplicableParameter> applicableParameters = []
    List<ParameterHolder> parameterHolders

    RuntimeParameterCollector(List<ParameterHolder> parameterHolders) {
        this.parameterHolders = parameterHolders
    }

    void visitModel(Model model) {
    }

    void visitComponent(Component component, ModelPath path) {
        createApplicableParameters(component)
    }

    protected createApplicableParameters(def component) {
        for (Map.Entry<String, Object> entry in GroovyUtils.getProperties(component)) {
            final String propertyName = entry.key
            if (propertyName.startsWith(RUNTIME_PARAMETER_PREFIX)) {
                final ParameterHolder runtimeParameter = parameterHolders.find { it.path == propertyName }
                if (runtimeParameter != null) {
                    applicableParameters << new ApplicableParameter(component: component, parameterPropertyName: propertyName, parameterValue: runtimeParameter.getBusinessObject())
                } else {
                    LOG.warn "No runtime paramater found for $propertyName"
                }
            }
        }
    }

    void visitResource(IResource resource, ModelPath path) {
        createApplicableParameters(resource)
    }

    void visitParameterObject(IParameterObject parameterObject, ModelPath path) {
    }


}
