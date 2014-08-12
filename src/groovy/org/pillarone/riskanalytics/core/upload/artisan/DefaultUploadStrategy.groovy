package org.pillarone.riskanalytics.core.upload.artisan

import groovy.util.logging.Log
import org.pillarone.riskanalytics.core.queue.IQueueTaskFuture
import org.pillarone.riskanalytics.core.remoting.IUploadService
import org.pillarone.riskanalytics.core.remoting.UploadInfo
import org.pillarone.riskanalytics.core.remoting.UploadException
import org.pillarone.riskanalytics.core.upload.IUploadStrategy
import org.pillarone.riskanalytics.core.upload.UploadConfiguration
import org.pillarone.riskanalytics.core.upload.UploadQueueTaskContext

import java.util.logging.Level

/**
 * Created with IntelliJ IDEA.
 * User: frahman
 * Date: 12/08/14
 * Time: 11:33an
 * To change this template use File | Settings | File Templates.
 */
@Log
class DefaultUploadStrategy implements IUploadStrategy {
    def backgroundService

    IUploadService uploadService

    @Override
    IQueueTaskFuture upload(UploadQueueTaskContext context, int priority) {
        UploadInfo uploadInfo = createUploadInfo(context.configuration)
        UploadTaskFuture future = new UploadTaskFuture(context, uploadInfo.uuid)
        backgroundService.execute("upload $uploadInfo") {
        }
        //this call has to block
        try {
            uploadService.startUpload(uploadInfo)
            future.done()
        } catch (UploadException uploadException) {
            log.log(Level.WARNING, "upload failed for $uploadInfo", uploadException)
            future.failed(uploadException.errors)
        }
        return future
    }

    private static UploadInfo createUploadInfo(UploadConfiguration configuration) {
        new UploadInfo(UUID.randomUUID(), configuration.simulation.id, null, configuration.allowOverwrite, configuration.destination)
    }
}
