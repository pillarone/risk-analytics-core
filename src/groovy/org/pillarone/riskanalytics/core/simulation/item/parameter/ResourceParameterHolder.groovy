package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ResourceParameter
import org.pillarone.riskanalytics.core.simulation.item.Resource

class ResourceParameterHolder extends ParameterHolder {

    Resource resource

    ResourceParameterHolder(Parameter parameter) {
        super(parameter)
    }

    ResourceParameterHolder(String path, int periodIndex, Resource resource) {
        super(path, periodIndex)
        this.resource = resource
    }

    @Override
    void applyToDomainObject(Parameter parameter) {
        parameter = parameter as ResourceParameter
        parameter.name = resource.name
        parameter.itemVersion = resource.versionNumber.toString()
        parameter.resourceClassName = resource.modelClass.name
    }

    @Override
    void setParameter(Parameter parameter) {
        parameter = parameter as ResourceParameter
        resource = new Resource(parameter.name, Thread.currentThread().contextClassLoader.loadClass(parameter.resourceClassName))
        resource.load(false)
    }

    @Override
    Object getBusinessObject() {
        return resource.resourceInstance
    }

    @Override
    protected void updateValue(Object newValue) {

    }

    @Override
    Parameter createEmptyParameter() {
        return new ResourceParameter(path: path, periodIndex: periodIndex)
    }
}
