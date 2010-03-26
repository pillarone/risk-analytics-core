package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter

class ExampleParameterObjectClassifier extends AbstractParameterObjectClassifier {

    static Map types = [:]

    public static final ExampleParameterObjectClassifier TYPE0 = new ExampleParameterObjectClassifier(
            "TYPE0", ["a": 10d, "b": 100d]
    )

    public static final ExampleParameterObjectClassifier TYPE1 = new ExampleParameterObjectClassifier(
            "TYPE1", ["p1": 0d, "p2": 1d]
    )

    public static final ExampleParameterObjectClassifier TYPE2 = new ExampleParameterObjectClassifier(
            "TYPE2", ["p1": 0d, "p2": 1d, "p3": "string"]
    )

    public static final ExampleParameterObjectClassifier NESTED_PARAMETER_OBJECT = new ExampleParameterObjectClassifier(
            "NESTED_PARAMETER_OBJECT", ["nested": TYPE0.getParameterObject(TYPE0.parameters)]
    )

    public static final ExampleParameterObjectClassifier NESTED_MDP = new ExampleParameterObjectClassifier(
            "NESTED_MDP", ["mdp": new SimpleMultiDimensionalParameter([[1, 2], [5, 6]])]
    )

    public static ExampleParameterObjectClassifier valueOf(String type) {
        types[type]
    }

    private ExampleParameterObjectClassifier(String classifier, Map parameters) {
        super(classifier, parameters)
        types[classifier] = this
    }

    List<IParameterObjectClassifier> getClassifiers() {
        return [TYPE0, TYPE1, TYPE2, NESTED_PARAMETER_OBJECT, NESTED_MDP];
    }

    IParameterObject getParameterObject(Map parameters) {
        return new ExampleParameterObject(classifier: this, parameters: parameters)
    }

    public static ExampleParameterObject getStrategy(ExampleParameterObjectClassifier classifier, Map parameters) {
        return new ExampleParameterObject(classifier: classifier, parameters: parameters)
    }

}