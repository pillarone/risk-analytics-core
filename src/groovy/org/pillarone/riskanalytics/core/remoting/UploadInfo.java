package org.pillarone.riskanalytics.core.remoting;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.UUID;

/**
 * Allianz Risk Transfer  ATOM
 */
public class UploadInfo implements Serializable {

    private static final long serialVersionUID = -2368426722415786382L;
    private UUID uuid;

    public UploadInfo(UUID uuid) {
        Preconditions.checkNotNull(uuid);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
