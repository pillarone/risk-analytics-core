package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IResult

class UploadResult implements IResult {
    List<String> errors = []
    UploadState uploadState = UploadState.PENDING
}
