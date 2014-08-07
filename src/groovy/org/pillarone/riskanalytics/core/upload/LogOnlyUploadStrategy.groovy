package org.pillarone.riskanalytics.core.upload

import models.core.CoreModel
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture

class LogOnlyUploadStrategy implements IUploadStrategy {
    private final static Log LOG = LogFactory.getLog(BasicQueueTaskFuture)

    def backgroundService

    @Override
    IQueueTaskFuture upload(UploadQueueTaskContext context, int priority) {
        BasicQueueTaskFuture future = new BasicQueueTaskFuture(context)
        backgroundService.execute('upload') {
            LOG.debug("start upload simulation: ${context.configuration.simulation}")
            Thread.sleep(1000)
            if (future.canceled) {
                LOG.debug("task was canceled -> exit")
                return
            }
            LOG.debug("finished uploading simulation: ${context.configuration.simulation}")
            if (context.configuration.simulation.modelClass == CoreModel) {
                future.failed(['upload not possible with core model'])
            } else {
                future.done()
            }
        }
        future
    }
}
