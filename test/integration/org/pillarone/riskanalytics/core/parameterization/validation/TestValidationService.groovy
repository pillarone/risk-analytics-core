package org.pillarone.riskanalytics.core.parameterization.validation

import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.StringParameterHolder

class TestValidationService implements IParameterizationValidator {

    List<ParameterValidation> validate(List<ParameterHolder> parameters) {
        List<ParameterValidation> errors = []

        for (ParameterHolder param in parameters) {
            if (param instanceof StringParameterHolder) {
                if (param.businessObject == "INVALID") {
                    errors << new TestError(ValidationType.ERROR, "invalid", [])
                }
            }
        }

        return errors
    }


}

class TestError extends ParameterValidation {

    def TestError(ValidationType validationType, message, arguments) {
        super(validationType, message, arguments);
    }

    String getLocalizedMessage(Locale locale) {
        return msg;
    }

}
