package org.pillarone.riskanalytics.core.parameterization

import org.codehaus.groovy.runtime.StackTraceUtils

public abstract class ParameterizationError extends Exception {

    public ParameterizationError(cause) {
        super(StackTraceUtils.sanitize(cause))
    }

    public String getMessage() {
        cause.message
    }

    int getLineNumber() {
        try {
            return cause.stackTrace.find {StackTraceElement element -> element.fileName?.startsWith("script")}.lineNumber
            //catch all exceptions here, otherwise the exception safe error message might get swallowed
        } catch (Exception e) {
            return -1
        }
    }
}

public class ParameterizationSaveError extends ParameterizationError {
    public ParameterizationSaveError(cause) {
        super(cause);
    }
}

public class ParameterizationImportError extends ParameterizationError {
    public ParameterizationImportError(cause) {
        super(cause);
    }
}