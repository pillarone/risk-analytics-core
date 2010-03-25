package org.pillarone.riskanalytics.core.parameterization

interface IParameterObject {
    IParameterObjectClassifier getType()

    Map getParameters()

}