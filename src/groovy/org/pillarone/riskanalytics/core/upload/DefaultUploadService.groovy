package org.pillarone.riskanalytics.core.upload

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.remoting.IUploadService
import org.pillarone.riskanalytics.core.remoting.UploadInfo
import org.pillarone.riskanalytics.core.remoting.UploadException

class DefaultUploadService implements IUploadService {
    private static final Log LOG = LogFactory.getLog(DefaultUploadService)

    @Override
    void startUpload(UploadInfo uploadInfo) throws UploadException {
        LOG.info("started upload for $uploadInfo")
        sleep(5000)
    }

    @Override
    void cancelUpload(UUID uploadInfoId) {
        LOG.info("canceled upload $uploadInfoId")
    }

}
