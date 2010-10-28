package models.core

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

model = models.core.CoreModel
displayName = 'coreParamWithoutComments'
periodCount = 1

components {
    exampleInputOutputComponent {
        parmParameterObject[0] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
    }
    dynamicComponent {
        subSubcomponent {
            parmParameterObject[0] = ExampleParameterObjectClassifier.TYPE0.getParameterObject(["a": 0d, "b": 1d])
        }
    }
}
