package org.pillarone.riskanalytics.core.example.parameter

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.ResourceHolder
import org.pillarone.riskanalytics.core.example.component.ExampleResource
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory

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

    public static final ExampleParameterObjectClassifier TYPE3 = new ExampleParameterObjectClassifier(
            "TYPE3", ["p1": 0d, "p2": 1d, "p3": 2d]
    )

    public static final ExampleParameterObjectClassifier NESTED_PARAMETER_OBJECT = new ExampleParameterObjectClassifier(
            "NESTED_PARAMETER_OBJECT", ["nested": TYPE0.getParameterObject(TYPE0.parameters)]
    )

    public static final ExampleParameterObjectClassifier NESTED_MDP = new ExampleParameterObjectClassifier(
            "NESTED_MDP", ["mdp": new SimpleMultiDimensionalParameter([[1, 2], [5, 6]])]
    )

    public static final ExampleParameterObjectClassifier NESTED_MDP2 = new ExampleParameterObjectClassifier(
            "NESTED_MDP2", ["mdp": new ConstrainedMultiDimensionalParameter([],['1','2'], ConstraintsFactory.getConstraints("ExampleMultiMarkerConstraint"))]
    )

    public static final ExampleParameterObjectClassifier RESOURCE = new ExampleParameterObjectClassifier(
            "RESOURCE", ["resource": new ConstrainedMultiDimensionalParameter([[new ResourceHolder(ExampleResource, "a", new VersionNumber("1"))]], ['title'], ConstraintsFactory.getConstraints(ExampleResourceConstraints.IDENTIFIER))]
    )

    public static ExampleParameterObjectClassifier valueOf(String type) {
        types[type]
    }

    private ExampleParameterObjectClassifier(String classifier, Map parameters) {
        super(classifier, parameters)
        types[classifier] = this
    }

    List<IParameterObjectClassifier> getClassifiers() {
        return [TYPE0, TYPE1, TYPE2, NESTED_PARAMETER_OBJECT, NESTED_MDP, NESTED_MDP2, RESOURCE];
    }

    IParameterObject getParameterObject(Map parameters) {
        return new ExampleParameterObject(classifier: this, parameters: parameters)
    }

    public static ExampleParameterObject getStrategy(ExampleParameterObjectClassifier classifier, Map parameters) {
        return new ExampleParameterObject(classifier: classifier, parameters: parameters)
    }

}