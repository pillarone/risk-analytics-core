package org.pillarone.riskanalytics.core.report

import groovy.transform.CompileStatic

/**
 * This exception is thrown when report parameters are wrong, such as a wrong selection 
 *
 * Author: bzetterstrom
 */
@CompileStatic
class UnsupportedReportParameterException extends RuntimeException {

    UnsupportedReportParameterException(String message) {
        super(message)
    }

    UnsupportedReportParameterException(String message, Throwable cause) {
        super(message, cause)
    }
}
