package org.pillarone.riskanalytics.core.parameterization.validation

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.StringParameterHolder


class TestValidationService implements IParameterizationValidator {

    List<ParameterValidationError> validate(List<ParameterHolder> parameters) {
        List<ParameterValidationError> errors = []

        for (ParameterHolder param in parameters) {
            if (param instanceof StringParameterHolder) {
                if (param.businessObject == "INVALID") {
                    errors << new TestError("invalid", [])
                }
            }
        }

        return errors
    }


}

class TestError extends ParameterValidationError {

    def TestError(message, arguments) {
        super(message, arguments);
    }

    String getLocalizedMessage(Locale locale) {
        return msg;
    }

}
