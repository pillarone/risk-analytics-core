package org.pillarone.riskanalytics.core.parameterization.validation

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.domain.utils.validation.ParameterValidationServiceImpl

/**
 * @author detlef.brendle@canoo.com
 */
abstract class AbstractParameterizationValidator implements IParameterizationValidator {
    private AbstractParameterValidationService validationService

    private Log LOG = LogFactory.getLog(getClass())


    AbstractParameterizationValidator() {
        validationService = new ParameterValidationServiceImpl()
        registerConstraints(validationService)
    }

    List<ParameterValidation> validate(List<ParameterHolder> parameters) {

        List<ParameterValidation> errors = []
        for (ParameterHolder parameter in parameters) {
            ParameterObjectParameterHolder parameterToVerify = verifyParameter(parameter)
            if (parameterToVerify) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug "validating ${parameterToVerify.path}"
                }
                def currentErrors = validationService.validate(parameterToVerify.classifier, getParameterMap(parameterToVerify))
                currentErrors*.path = getErrorPath(parameterToVerify)
                errors.addAll(currentErrors)
            }
        }
        return errors
    }

    protected Map getParameterMap(ParameterObjectParameterHolder parameterToVerify) {
        parameterToVerify.getParameterMap()
    }

    protected String getErrorPath(ParameterObjectParameterHolder parameter) {
        parameter.path
    }

    abstract void registerConstraints(AbstractParameterValidationService validationService)

    /**
     * Returns null if the input parameter must not be verified or a ParameterObjectParameterHolder parameter on which the verification takes place.
     * @param parameter
     * @return
     */
    abstract ParameterObjectParameterHolder verifyParameter(ParameterHolder parameter)

}
