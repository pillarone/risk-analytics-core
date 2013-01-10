package org.pillarone.riskanalytics.core.parameterization.validation

/**
 https://issuetracking.intuitive-collaboration.com/jira/browse/PMO-40
 */
abstract class AbstractParameterValidationService {

    boolean transactional = false

    protected Map validators = [:] // maps keys to a list of validating closures

    /**
     * @param classifier is the object used to find the validation closure for a candidate object in the registry.
     * Objects are classified if they are <ul>
     * <li>identical (in the case of singletons) to the classifier object,
     * of the classifier class,</li>
     * <li>equal to the classifier, or if</li>
     * <li>classifier.isCase(obj) is true.</li></ul>
     * @param validator
     * Set the validator param to a Closure that receives the object to be validated.
     * The closure can return: <ul>
     * <li>null or true to indicate that the object is valid</li>
     * <li>a list containing a string describing the error code to be looked up in the i18n bundle,
     * and then any number of arguments following it,
     * which can be used as formatted message arguments indexed at 0 onwards.</li></ul>
     */
    def register(Object classifier, Closure validator) {
        validators.get(classifier, []) << validator
    }

    /**
     * @return list of validation errors or null if candidate is ok
     */
    List<ParameterValidation> validate(Object classifier, Object candidate, Map extraInfo = null) {
        List errors = []
        for (validator in findValidators(classifier)) {
            def result
            if(extraInfo) {
                result = validator(candidate, extraInfo)
            } else {
                result = validator(candidate)
            }
            if (null == result || true == result) continue  // validation ok
            if (!(result[0] instanceof ValidationType)) throw new IllegalArgumentException("${result} doesn't contains ValidationType")
            ValidationType validationType = result.remove(0 as int)
            String messageKey = result.remove(0 as int).toString()
            errors << createErrorObject(validationType, messageKey, result)
        }
        return errors
    }

    abstract ParameterValidation createErrorObject(ValidationType validationType, String msg, List args)

    /**
     * Convenience method for validate(obj, obj) when obj is its own classifier.
     */
    List<ParameterValidation> validate(Object candidate) {
        validate(candidate, candidate)
    }

    List findValidators(candidate) {
        def result
        result = validators[candidate]                  // if the object itself is registered as it is e.g. a singleton
        if (result) return result
        result = validators[candidate.class]                                                // the class is registered
        if (result) return result
        result = validators.keySet().toList().findAll { candidate == it }                    // find equal keys
        if (result) return result.collect { validators[it] }.flatten()
        result = validators.keySet().toList().findAll { it.isCase(candidate) }               // find by isCase()
        if (result) return result.collect { validators[it] }.flatten()
        return Collections.EMPTY_LIST
    }
}

