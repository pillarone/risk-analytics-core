package models.core

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

model = models.core.CoreModel
periodCount = 2

components {
    exampleInputOutputComponent {
        parmParameterObject[0] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
        parmParameterObject[1] = ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 1d])
    }
    dynamicComponent {
        subSubcomponent {
            parmParameterObject[0] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
            parmParameterObject[1] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
        }
    }
}
