package org.pillarone.riskanalytics.core.parameterization

interface IParameterObject {
    Object getType() // todo (dk, msp) make this a AbstractParameterObjectClassifier

    Map getParameters()

}