package org.pillarone.riskanalytics.core.upload

interface IUploadStrategy {

    IUploadFuture upload(UploadConfiguration configuration, int priority)
}
