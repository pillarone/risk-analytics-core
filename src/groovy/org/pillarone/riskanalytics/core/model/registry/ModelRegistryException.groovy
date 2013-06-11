package org.pillarone.riskanalytics.core.model.registry

import groovy.transform.CompileStatic

@CompileStatic
class ModelRegistryException extends RuntimeException {

    ModelRegistryException() {
    }

    ModelRegistryException(Throwable cause) {
        super(cause)
    }

    ModelRegistryException(String message) {
        super(message)
    }

    ModelRegistryException(String message, Throwable cause) {
        super(message, cause)
    }
}
