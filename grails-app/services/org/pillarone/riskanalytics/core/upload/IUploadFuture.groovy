package org.pillarone.riskanalytics.core.upload

interface IUploadFuture {
    void stopListenAsync(IUploadTaskListener taskListener)

    void listenAsync(IUploadTaskListener uploadTaskListener)

    void cancel()

    UploadResult getUploadResult()

}
