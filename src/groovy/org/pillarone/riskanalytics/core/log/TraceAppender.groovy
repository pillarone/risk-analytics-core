package org.pillarone.riskanalytics.core.log

import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent

@CompileStatic
class TraceAppender extends AppenderSkeleton {

    @Lazy TraceLogManager logManager = { Holders.grailsApplication.mainContext.getBean(TraceLogManager) }()

    @Override
    protected void append(LoggingEvent loggingEvent) {
        logManager.log(layout.format(loggingEvent))
    }

    @Override
    void close() {

    }

    @Override
    boolean requiresLayout() {
        return true
    }
}
