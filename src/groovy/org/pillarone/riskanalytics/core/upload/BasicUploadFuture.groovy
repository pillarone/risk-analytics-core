package org.pillarone.riskanalytics.core.upload

import java.util.concurrent.CopyOnWriteArraySet

class BasicUploadFuture implements IUploadFuture {

    private UploadResult uploadResult
    private volatile boolean canceled = false
    private final Set<IUploadTaskListener> taskListeners = new CopyOnWriteArraySet<IUploadTaskListener>()


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
    UploadResult getUploadResult() {
        uploadResult
    }

    @Override
    void stopListenAsync(IUploadTaskListener uploadTaskListener) {
        taskListeners.remove(uploadTaskListener)
    }

    @Override
    void listenAsync(IUploadTaskListener uploadTaskListener) {
        taskListeners.add(uploadTaskListener)
    }
}
