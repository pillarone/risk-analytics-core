package org.pillarone.riskanalytics.core.parameterization

/**
 https://issuetracking.intuitive-collaboration.com/jira/browse/PMO-40
 */
class ParameterValidationService {

    boolean transactional = false

    private Map validators = [:] // maps keys to a list of validating closures

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
    List<ParameterValidationError> validate(Object classifier, Object candidate) {
        List errors = null
        for (validator in findValidators(classifier)) {
            def result = validator(candidate)
            if (null == result || true == result) continue  // validation ok
            if (null == errors) errors = []                 // lazy init for performance
            errors << new ParameterValidationError(classifier.toString(), result)
        }
        return errors
    }

    /**
     * Convenience method for validate(obj, obj) when obj is its own classifier.
     */
    List<ParameterValidationError> validate(Object candidate) {
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

class ParameterValidationError {
    final String classifier
    final List args
    final String msg
    String path

    ParameterValidationError(String classifierStr, List msgWithArgs) {
        classifier = classifierStr
        msg = msgWithArgs.remove(0) // better not modifying the list?
        args = msgWithArgs
    }

    String toString() {
        "Error '$msg' at $path with args $args for classifier $classifier"
    }
    // todo: localizedMessage
}