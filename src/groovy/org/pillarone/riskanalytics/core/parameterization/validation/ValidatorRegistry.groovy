package org.pillarone.riskanalytics.core.parameterization.validation

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class ValidatorRegistry {

    private static Log LOG = LogFactory.getLog(ValidatorRegistry)

    private static List<IParameterizationValidator> validators = new LinkedList<IParameterizationValidator>()

    static void addValidator(IParameterizationValidator validator) {
        if (!validators.contains(validator)) {
            validators.add(validator)
        } else {
            LOG.warn "Validator ${validator.class.simpleName} already exists - ignoring"
        }
    }

    static List<IParameterizationValidator> getValidators() {
        return Collections.unmodifiableList(validators)
    }
}
