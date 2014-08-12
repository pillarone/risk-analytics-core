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
    private long simulationId;
    private String quarter;
    private boolean allowOverwrite;
    private String destination;
    private String username;

    public UploadInfo(UUID uuid, long simulationId, String quarter, boolean allowOverwrite, String destination, String username) {
        this.simulationId = simulationId;
        this.quarter = quarter;
        this.allowOverwrite = allowOverwrite;
        this.destination = destination;
        this.username = username;
        Preconditions.checkNotNull(uuid);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public long getSimulationId() {
        return simulationId;
    }

    public String getQuarter() {
        return quarter;
    }

    public boolean isAllowOverwrite() {
        return allowOverwrite;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
