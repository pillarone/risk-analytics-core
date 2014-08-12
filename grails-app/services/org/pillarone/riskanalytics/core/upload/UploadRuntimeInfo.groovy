package org.pillarone.riskanalytics.core.upload

import com.google.common.base.Preconditions
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.queue.IRuntimeInfo
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.user.Person

class UploadRuntimeInfo implements Comparable<UploadRuntimeInfo>, IRuntimeInfo<UploadQueueEntry> {

    private Simulation simulation
    private Integer priority
    private Date offeredAt
    private UUID id
    private Integer progress
    private UploadState uploadState
    private String username
    private List<String> uploadErrors
    private DateTime start
    private DateTime end
    private DateTime estimatedEnd
    private long offeredNanoTime

    UploadRuntimeInfo(UUID id) {
        this.id = Preconditions.checkNotNull(id)
    }

    UploadRuntimeInfo(UploadQueueEntry entry) {
        this(entry.id)
        apply(entry)
    }

    @Override
    int compareTo(UploadRuntimeInfo o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
        }
        return priority.compareTo(o.priority)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        UploadRuntimeInfo that = (UploadRuntimeInfo) o
        if (id != that.id) return false
        return true
    }

    int hashCode() {
        return id.hashCode()
    }

    @Override
    boolean apply(UploadQueueEntry entry) {
        boolean changed = false
        if (entry.id != id) {
            throw new IllegalStateException("queueEntry id is different from our id")
        }
        simulation = entry.context.configuration.simulation
        offeredNanoTime = entry.offeredNanoTime

        if (priority != entry.priority) {
            priority = entry.priority
            changed = true
        }
        if (offeredAt != entry.offeredAt) {
            offeredAt = entry.offeredAt
            changed = true
        }
        if (progress != entry.context.progress) {
            progress = entry.context.progress
            changed = true
        }
        if (uploadState != entry.context.uploadState) {
            uploadState = entry.context.uploadState
            changed = true
        }
        if (estimatedEnd != entry.context.estimatedEnd) {
            estimatedEnd = entry.context.estimatedEnd
            changed = true
        }
        if (username != entry.context.username) {
            username = entry.context.username
            changed = true
        }
        if (uploadErrors != entry.context.errors) {
            uploadErrors = entry.context.errors
            changed = true
        }
        changed
    }

    @Override
    String getUsername() {
        username
    }

    @Override
    UUID getId() {
        id
    }

    Simulation getSimulation() {
        simulation
    }

    Parameterization getParameterization() {
        simulation?.parameterization
    }

    ResultConfiguration getResultConfiguration() {
        simulation?.template
    }

    Integer getIterations() {
        simulation?.numberOfIterations
    }

    Integer getPriority() {
        priority
    }

    Date getConfiguredAt() {
        offeredAt
    }

    Integer getProgress() {
        progress
    }

    UploadState getUploadState() {
        uploadState
    }

    DateTime getStart() {
        start
    }

    List<String> getUploadErrors() {
        uploadErrors
    }

    DateTime getEnd() {
        end
    }

    @Override
    DateTime getEstimatedEnd() {
        estimatedEnd
    }
}
