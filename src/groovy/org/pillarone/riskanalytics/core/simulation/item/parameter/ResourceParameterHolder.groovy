package org.pillarone.riskanalytics.core.simulation.item.parameter

import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ResourceParameter
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class ResourceParameterHolder extends ParameterHolder {

    String name
    String version
    Class resourceClass


    ResourceParameterHolder(Parameter parameter) {
        super(parameter)
    }

    ResourceParameterHolder(String path, int periodIndex, Resource resource) {
        super(path, periodIndex)
        name = resource.name
        version = resource.versionNumber?.toString()
        resourceClass = resource.modelClass
    }

    @Override
    void applyToDomainObject(Parameter parameter) {
        parameter.name = name
        parameter.itemVersion = version
        parameter.resourceClassName = resourceClass.name
    }

    @Override
    void setParameter(Parameter parameter) {
        name = parameter.name
        resourceClass = Thread.currentThread().contextClassLoader.loadClass(parameter.resourceClassName)
        version = parameter.itemVersion
    }

    @Override
    Object getBusinessObject() {
        Resource resource = new Resource(name,resourceClass)
        if (version != null) {
            resource.versionNumber = new VersionNumber(version)
        }
        return resource.resourceInstance
    }

    @Override
    protected void updateValue(Object newValue) {
        NameVersionPair pair = newValue as NameVersionPair
        name = pair.name
        version = pair.version
    }

    @Override
    Parameter createEmptyParameter() {
        return new ResourceParameter(path: path, periodIndex: periodIndex)
    }

    public static class NameVersionPair {
        String name
        String version

        NameVersionPair(String name, String version) {
            this.name = name
            this.version = version
        }

        @Override
        String toString() {
            return "${name} v${version}"
        }

    }
}
