package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

abstract class AbstractParameterObjectClassifier implements IParameterObjectClassifier {

    final Map parameters
    protected String displayName
    protected String typeName


    public AbstractParameterObjectClassifier(String typeName, Map parameters) {
        this(typeName, typeName, parameters)
    }

    public AbstractParameterObjectClassifier(String displayName, String typeName, Map parameters) {
        this.displayName = displayName
        this.typeName = typeName
        this.parameters = parameters
    }

    public List getParameterNames() {
        parameters.keySet().toList()
    }

    public getType(Object parameterName) {
        parameters[parameterName]
    }

    public String toString() {
        displayName
    }

    public def propertyMissing(String propName){
        getParameters().get(propName)
    }

}