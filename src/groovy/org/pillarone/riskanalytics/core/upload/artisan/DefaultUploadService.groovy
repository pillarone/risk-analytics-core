package org.pillarone.riskanalytics.core.upload.artisan

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.remoting.IUploadService
import org.pillarone.riskanalytics.core.remoting.impl.UploadException

class DefaultUploadService implements IUploadService {
    private static final Log LOG = LogFactory.getLog(DefaultUploadService)

    @Override
    void startUpload(UUID uuid, long simulationId, String quarter, boolean allowOverwrite, String destination) throws UploadException {
        LOG.info("started upload for $uuid, $simulationId, $quarter, $allowOverwrite, $destination")
        sleep(5000)
    }

    @Override
    void cancelUpload(UUID uploadInfoId) {
        LOG.info("canceled upload $uploadInfoId")
    }

}
