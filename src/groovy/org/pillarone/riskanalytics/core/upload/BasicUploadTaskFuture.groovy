package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.queue.IQueueTaskListener

import java.util.concurrent.CopyOnWriteArraySet

class BasicUploadTaskFuture implements IQueueTaskFuture {

    private final UploadQueueTaskContext context
    private volatile boolean canceled = false
    private final Set<IQueueTaskListener> taskListeners = new CopyOnWriteArraySet<IQueueTaskListener>()

    BasicUploadTaskFuture(UploadQueueTaskContext context) {
        this.context = context
    }

    @Override
    void cancel() {
        canceled = true
        context.uploadState = UploadState.CANCELED
        notifyUploadListeners()
    }

    void failed(List<String> errors) {
        context.uploadState = UploadState.ERROR
        errors.each { context.addError(it) }
        notifyUploadListeners()
    }

    void done() {
        context.uploadState = UploadState.DONE
        notifyUploadListeners()
    }

    private notifyUploadListeners() {
        synchronized (taskListeners) {
            taskListeners.each { it.apply(this) }
        }
    }

    boolean getCanceled() {
        canceled
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
