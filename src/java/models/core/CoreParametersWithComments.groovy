package models.core

import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier

model = models.core.CoreModel
displayName = 'coreParamTest'
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
comments = ["[path:'Core:exampleInputOutputComponent:parmParameterObject', period:0, lastChange:new java.util.Date(1285144738000),user:null, comment: 'comment text', tags:(['FIXED'] as Set)]"]
