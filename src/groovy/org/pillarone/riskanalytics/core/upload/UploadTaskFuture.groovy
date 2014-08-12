package org.pillarone.riskanalytics.core.upload

import grails.util.Holders
import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.queue.IQueueTaskListener
import org.pillarone.riskanalytics.core.remoting.IUploadService
import org.pillarone.riskanalytics.core.upload.UploadQueueTaskContext
import org.pillarone.riskanalytics.core.upload.UploadState

import java.util.concurrent.CopyOnWriteArraySet

class UploadTaskFuture implements IQueueTaskFuture {

    private final UploadQueueTaskContext context
    private volatile boolean canceled = false
    private final Set<IQueueTaskListener> taskListeners = new CopyOnWriteArraySet<IQueueTaskListener>()
    private UUID uploadInfoId

    UploadTaskFuture(UploadQueueTaskContext context, UUID uploadInfoId) {
        this.uploadInfoId = uploadInfoId
        this.context = context
    }

    @Override
    void cancel() {
        canceled = true
        uploadService.cancelUpload(uploadInfoId)
        context.uploadState = UploadState.CANCELED
        notifyUploadListeners()
    }

    void failed(List<String> errors) {
        if (canceled) {
            return
        }
        context.uploadState = UploadState.ERROR
        errors.each { context.addError(it) }
        notifyUploadListeners()
    }

    void done() {
        if (canceled) {
            return
        }
        context.uploadState = UploadState.DONE
        context.progress = 100
        notifyUploadListeners()
    }

    private notifyUploadListeners() {
        synchronized (taskListeners) {
            taskListeners.each { it.apply(this) }
        }
    }

    static IUploadService getUploadService() {
        Holders.grailsApplication.mainContext.getBean('uploadService', IUploadService)
    }

    @Override
    void stopListenAsync(IQueueTaskListener uploadTaskListener) {
        taskListeners.remove(uploadTaskListener)
    }

    @Override
    void listenAsync(IQueueTaskListener uploadTaskListener) {
        taskListeners.add(uploadTaskListener)
    }
}
