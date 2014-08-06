package org.pillarone.riskanalytics.core.upload

interface IUploadTaskListener {
    void apply(IUploadFuture future)
}