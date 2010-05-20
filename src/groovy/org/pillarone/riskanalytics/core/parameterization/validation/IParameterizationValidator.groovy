package org.pillarone.riskanalytics.core.parameterization.validation

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder


interface IParameterizationValidator {

    List<ParameterValidationError> validate(List<ParameterHolder> parameters)

}
