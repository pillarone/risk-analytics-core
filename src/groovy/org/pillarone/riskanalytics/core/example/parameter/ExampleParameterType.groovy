package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ExampleParameterType extends AbstractParameterObjectClassifier implements Serializable {

    public static final ExampleParameterType TYPE_ONE = new ExampleParameterType(
            "type one", "TYPE_ONE", ["classifier1": 1d])
    public static final ExampleParameterType TYPE_TWO = new ExampleParameterType(
            "type two", "TYPE_TWO", ["classifier1": 1d, "classifier2": 2d])

    public static final all = [ TYPE_ONE, TYPE_TWO ]

    protected static Map types = [:]

    static {
        ExampleParameterType.all.each {
            ExampleParameterType.types[it.toString()] = it
        }
    }

    protected ExampleParameterType(String typeName, Map parameters) {
        this(typeName, typeName, parameters)
    }

    protected ExampleParameterType(String displayName, String typeName, Map parameters) {
        super(displayName, typeName, parameters)
    }

    public static ExampleParameterType valueOf(String type) {
        types[type]
    }

    public List<IParameterObjectClassifier> getClassifiers() {
        all
    }

    public IParameterObject getParameterObject(Map parameters) {
        return ExampleParameterType.getStrategy(this, parameters)
    }

    static IExampleParameterStrategy getDefault() {
        return ExampleParameterType.getStrategy(ExampleParameterType.TYPE_ONE, ['classifier1': 0d])
    }

    public String getConstructionString(Map parameters) {
        TreeMap sortedParameters = new TreeMap()
        sortedParameters.putAll(parameters)
        "org.pillarone.riskanalytics.core.example.parameter.ExampleParameterType.getStrategy(${this.class.name}.${typeName.toUpperCase()}, $sortedParameters)"
    }

    private Object readResolve() throws java.io.ObjectStreamException {
        return types[displayName]
    }


    static IExampleParameterStrategy getStrategy(ExampleParameterType type, Map parameters) {
        switch (type) {
            case ExampleParameterType.TYPE_ONE:
                return new ExampleParameterTypeOne(classifier1 : parameters['classifier1'])
            case ExampleParameterType.TYPE_TWO:
                return new ExampleParameterTypeTwo(classifier1 : parameters['classifier1'], classifier2 : parameters['classifier2'])
        }
    }
}
