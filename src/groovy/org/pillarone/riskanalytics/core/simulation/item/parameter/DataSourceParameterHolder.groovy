package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.parameter.DataSourceParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber


class DataSourceParameterHolder extends ParameterHolder {

    DataSourceDefinition definition

    DataSourceParameterHolder(Parameter parameter) {
        super(parameter)
    }

    DataSourceParameterHolder(String path, int periodIndex, DataSourceDefinition definition) {
        super(path, periodIndex)
        this.definition = definition
    }

    @Override
    void setParameter(Parameter parameter) {
        definition = new DataSourceDefinition()
        definition.parameterization = new Parameterization(parameter.parameterizationName, Thread.currentThread().contextClassLoader.loadClass(parameter.modelClassName))
        definition.parameterization.versionNumber = new VersionNumber(parameter.parameterizationVersion)

        definition.path = parameter.parameterizationPath
        definition.fields = parameter.fields.split("\\|")
        definition.periods = parameter.periods.split("\\|").collect { Integer.parseInt(it) }

        definition.collectorName = parameter.collectorName

    }

    @Override
    Object getBusinessObject() {
        return definition
    }

    @Override
    protected void updateValue(Object newValue) {

    }

    @Override
    void applyToDomainObject(Parameter parameter) {
        parameter.parameterizationName = definition.parameterization.name
        parameter.parameterizationVersion = definition.parameterization.versionNumber.toString()
        parameter.parameterizationPath = definition.path
        parameter.modelClassName = definition.parameterization.modelClass.name
        parameter.fields = definition.fields.join("|")
        parameter.periods = definition.periods.join("|")
        parameter.collectorName = definition.collectorName
    }

    @Override
    Parameter createEmptyParameter() {
        return new DataSourceParameter(path: path, periodIndex: periodIndex)
    }
}
