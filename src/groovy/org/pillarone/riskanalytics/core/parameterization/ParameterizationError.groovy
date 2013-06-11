package org.pillarone.riskanalytics.core.parameterization

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.StackTraceUtils

@CompileStatic
public abstract class ParameterizationError extends Exception {

    public ParameterizationError(Throwable cause) {
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

@CompileStatic
public class ParameterizationSaveError extends ParameterizationError {
    public ParameterizationSaveError(Throwable cause) {
        super(cause);
    }
}

@CompileStatic
public class ParameterizationImportError extends ParameterizationError {
    public ParameterizationImportError(Throwable cause) {
        super(cause);
    }
}