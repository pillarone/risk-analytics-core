package org.pillarone.riskanalytics.core.output.batch

import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.pillarone.riskanalytics.core.batch.BatchRunService
import org.quartz.Job
import org.quartz.JobExecutionContext

@CompileStatic
public class BatchRunner implements Job {

    static final Logger LOG = Logger.getLogger(BatchRunner)

    public static BatchRunService getService() {
        Holders.grailsApplication.mainContext.getBean(BatchRunService)
    }

    public void execute(JobExecutionContext jobExecutionContext) {
        synchronized (this) {
            getService().runBatches()
        }
    }

}


