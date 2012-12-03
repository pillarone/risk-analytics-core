package org.pillarone.riskanalytics.core.parameterization

import org.joda.time.DateTime

abstract class AbstractParameterObjectClassifier implements IParameterObjectClassifier {

    final transient Map parameters
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

    public def propertyMissing(String propName) {
        getParameters().get(propName)
    }

    public String getConstructionString(Map parameters) {
        StringBuffer parameterString = new StringBuffer('[')
        parameters.each {k, v ->
            if (v instanceof Enum) {
                parameterString << "\"$k\":${v.declaringClass.name}.$v,"
            }
            else if (v instanceof DateTime) {
                parameterString << "\"$k\":${createConstructionString(v)},"
            }
            else if (v instanceof IParameterObject) {
                parameterString << "\"$k\":${v.type.getConstructionString(v.parameters)},"
            }
            else if (v instanceof String) {
                parameterString << "\"$k\":\"$v\","
            }
            else {
                parameterString << "\"$k\":$v,"
            }
        }
        if (parameterString.size() == 1) {
            parameterString << ':'
        }
        parameterString << ']'
        String clazz = getClass().toString().replaceFirst("class\\s", "")
        return clazz + ".getStrategy(${this.class.name}.${typeName.toUpperCase()}, ${parameterString})"
    }

    protected String createConstructionString(DateTime dateTime) {
        StringBuilder builder = new StringBuilder("new org.joda.time.DateTime(")

        builder.append(dateTime.getYear()).append(", ").append(dateTime.getMonthOfYear()).append(", ").append(dateTime.getDayOfMonth()).append(", ").append(dateTime.getHourOfDay()).append(", ").append(dateTime.getMinuteOfHour()).append(", ").append(dateTime.getSecondOfMinute()).append(", ").append(dateTime.getMillisOfSecond())

        builder.append(")")
        return builder.toString()
    }

    String getTypeName() {
        typeName
    }
}