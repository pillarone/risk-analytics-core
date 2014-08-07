package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.queue.IQueueTaskListener

import java.util.concurrent.CopyOnWriteArraySet

class BasicQueueTaskFuture implements IQueueTaskFuture {

    private UploadResult uploadResult
    private volatile boolean canceled = false
    private final Set<IQueueTaskListener> taskListeners = new CopyOnWriteArraySet<IQueueTaskListener>()


    @Override
    void cancel() {
        canceled = true
        uploadResult = new UploadResult(uploadState: UploadState.CANCELED)
        notifyUploadListeners()
    }

    void failed(List<String> errors) {
        uploadResult = new UploadResult(uploadState: UploadState.ERROR, errors: errors)
        notifyUploadListeners()
    }

    void done() {
        uploadResult = new UploadResult(uploadState: UploadState.DONE)
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
    UploadResult getResult() {
        uploadResult
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
