package org.pillarone.riskanalytics.core.upload

import grails.transaction.Transactional

@Transactional
class UploadResultService {

    void upload(UploadConfiguration configuration, int priority = 5) {
        log.info("upload $configuration with priority $priority")
    }
}
