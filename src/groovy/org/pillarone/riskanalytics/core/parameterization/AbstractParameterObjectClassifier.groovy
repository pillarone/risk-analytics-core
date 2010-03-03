package org.pillarone.riskanalytics.core.parameterization

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

    public String getConstructionString(Map parameters) {
        StringBuffer parameterString = new StringBuffer('[')
        parameters.each {k, v ->
            if (v.class.isEnum()) {
                parameterString << "\"$k\":${v.class.name}.$v,"
            }
            else if (v instanceof IParameterObject) {
                parameterString << "\"$k\":${v.type.getConstructionString(v.parameters)},"
            }
            else {
                parameterString << "\"$k\":$v,"
            }
        }
        if (parameterString.size() == 1) {
            parameterString << ':'
        }
        parameterString << ']'
        String clazz = getClass().toString().replaceFirst("class\\s","")
        return clazz + ".getStrategy(${this.class.name}.${typeName.toUpperCase()}, ${parameterString})"
    }
}