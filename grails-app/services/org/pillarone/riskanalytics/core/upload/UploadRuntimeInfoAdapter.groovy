package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.queue.IRuntimeInfoListener

class UploadRuntimeInfoAdapter implements IRuntimeInfoListener<UploadRuntimeInfo> {
    @Override
    void starting(UploadRuntimeInfo info) {}

    @Override
    void finished(UploadRuntimeInfo info) {}

    @Override
    void removed(UploadRuntimeInfo info) {}

    @Override
    void offered(UploadRuntimeInfo info) {}

    @Override
    void changed(UploadRuntimeInfo info) {}
}
