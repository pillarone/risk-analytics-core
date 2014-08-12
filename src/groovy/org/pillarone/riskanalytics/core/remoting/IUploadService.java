package org.pillarone.riskanalytics.core.remoting;

import org.pillarone.riskanalytics.core.remoting.UploadException;

import java.util.UUID;

/**
 * Allianz Risk Transfer  ArtisanRemote
 * User: frahman
 * <p/>
 * This interface specifies the contract allowing uploading results to an external system.
 * An implementation must be provided to allow Artisan to upload results to PriceModelling.
 */
public interface IUploadService {

    /* Interim / scaffolding apis */


    /**
     * Upload sim and throw remote exception if problem occurs; returns handle that can be used for cancellation attempt.
     * @param uploadInfo
     */
    void startUpload(// 'destination' field in gui pfo nr  etc
                     UploadInfo uploadInfo) throws UploadException;

    void cancelUpload(UUID uploadInfoId);

}
