package org.pillarone.riskanalytics.core.parameterization.validation

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

interface IParameterizationValidator {

    List<ParameterValidation> validate(List<ParameterHolder> parameters)

}
