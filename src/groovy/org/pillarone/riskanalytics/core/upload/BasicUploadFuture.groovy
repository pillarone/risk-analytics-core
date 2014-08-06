package org.pillarone.riskanalytics.core.upload

class BasicUploadFuture implements IUploadFuture {

    private IUploadTaskListener taskListener
    private UploadResult uploadResult
    private volatile boolean canceled = false


    @Override
    void cancel() {
        canceled = true
        uploadResult = new UploadResult(uploadState: UploadState.CANCELED)
        notifyUploadListener()
    }

    void failed(List<String> errors) {
        uploadResult = new UploadResult(uploadState: UploadState.ERROR, errors: errors)
        notifyUploadListener()
    }

    void done() {
        uploadResult = new UploadResult(uploadState: UploadState.DONE)
        notifyUploadListener()
    }

    private notifyUploadListener() {
        taskListener.apply(this)
    }

    boolean getCanceled() {
        return canceled
    }

    @Override
    UploadResult getUploadResult() {
        return uploadResult
    }

    @Override
    void stopListenAsync(IUploadTaskListener uploadTaskListener) {
        taskListener = null
    }

    @Override
    void listenAsync(IUploadTaskListener uploadTaskListener) {
        taskListener = uploadTaskListener
    }
}
