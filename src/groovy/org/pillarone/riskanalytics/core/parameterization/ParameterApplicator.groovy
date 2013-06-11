package org.pillarone.riskanalytics.core.parameterization

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.DynamicComposedComponent
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.util.GroovyUtils

/**
 * The ParameterApplicator is responsible for writing the parameter values defined by the ParameterizationDAO to the model.
 * For each parameter an instance of ApplicableParameter is created. The ApplicableParameters are stored in lists
 * per period. Writing the parameter to the components is a simple iteration over the ApplicableParameters per period.
 *
 * As a sideeffect of creating the ApplicableParameter, the model is distributed to the AbstractMultiDimensionalParameter,
 * the selectedComponent is set on ConstrainedString parameter, missing subComponents of a DynamicComposedComponent get
 * created.
 */
public class ParameterApplicator {

    Model model
    Parameterization parameterization
    List<List<ApplicableParameter>> parameterPerPeriod
    int lastInjectedPeriod = -1

    /**
     * Creates the internal representation of all parameters defined by the ParameterizationDAO.
     */
    @TypeChecked
    void init() {
        assert model
        assert parameterization
        parameterPerPeriod = buildApplicableParameter(parameterization)
        for (List<ApplicableParameter> parameterForPeriod in parameterPerPeriod) {
            for (ApplicableParameter parameter in parameterForPeriod) {
                prepareParameter(model, parameter)
            }
        }
    }

    /**
     * Writing the parameter defined for the period to the models components.
     * ApplicableParameter.apply() is called for all parameter defined for the period.
     */
    @CompileStatic
    public void applyParameterForPeriod(int periodIndex) {
        List<ApplicableParameter> parameterForPeriod = parameterPerPeriod[periodIndex]
        if (parameterForPeriod != null) {
            for (ApplicableParameter parameter in parameterForPeriod) {
                parameter.apply()
            }
        }
        lastInjectedPeriod = periodIndex
    }


    @CompileStatic
    protected List<List<ApplicableParameter>> buildApplicableParameter(Parameterization parameterization) {
        List<List<ApplicableParameter>> parameterPerPeriod = []
        parameterization.periodCount.times {
            parameterPerPeriod << []
        }
        parameterization.parameterHolders.each {ParameterHolder p ->
            parameterPerPeriod[p.periodIndex] << createApplicableParameter(model, p)
        }

        return parameterPerPeriod
    }

    protected ApplicableParameter createApplicableParameter(Model model, ParameterHolder parameterHolder) {
        String path = parameterHolder.path
        def parameterValue = parameterHolder.businessObject

        def pathElements = path.split("\\:")
        def component = model
        pathElements[0..-2].each {propertyName ->
            component = getPropertyOrSubComponent(propertyName, component)
        }

        return new ApplicableParameter(component: component, parameterPropertyName: pathElements[-1], parameterValue: parameterValue)
    }

    @TypeChecked
    protected void prepareParameter(Model model, ApplicableParameter parameter) {
        prepareParameter(model, parameter.parameterValue, parameter.parameterPropertyName)
    }

    protected void prepareParameter(Model model, def parameterValue, String context) {}

    @CompileStatic
    protected void prepareParameter(Model model, AbstractMultiDimensionalParameter parameterValue, String context) {
        parameterValue.simulationModel = model
    }

    @TypeChecked
    protected void prepareParameter(Model model, IParameterObject parameterValue, String context) {
        if (parameterValue == null) {
            throw new IllegalStateException("Parameter object null in ${context}")
        }
        for (Map.Entry entry in parameterValue.getParameters().entrySet()) {
            prepareParameter(model, entry.value, context + ":" + entry.key.toString())
        }
    }

    @CompileStatic
    protected void prepareParameter(Model model, ConstrainedString parameterValue, String context) {
        List<Component> allMarkedComponents = model.getMarkedComponents(parameterValue.markerClass)
        parameterValue.selectedComponent = allMarkedComponents.find {Component c -> c.name == parameterValue.stringValue }
    }

    protected def getPropertyOrSubComponent(String propertyName, def component) {
        return component[propertyName]
    }

    protected def getPropertyOrSubComponent(String propertyName, DynamicComposedComponent component) {

        if (!GroovyUtils.getProperties(component).containsKey(propertyName)) {
            if (propertyName.startsWith("sub")) {
                Component subComponent = component.createDefaultSubComponent()
                subComponent.name = propertyName
                component.addSubComponent(subComponent)
            } else {
                throw new MissingPropertyException(propertyName, component.class)
            }
        }

        return component[propertyName]
    }
}
