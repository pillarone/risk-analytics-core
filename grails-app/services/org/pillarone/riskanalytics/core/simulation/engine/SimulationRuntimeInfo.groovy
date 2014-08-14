package org.pillarone.riskanalytics.core.simulation.engine

import com.google.common.base.Preconditions
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.pillarone.riskanalytics.core.queue.IRuntimeInfo
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class SimulationRuntimeInfo implements Comparable<SimulationRuntimeInfo>, IRuntimeInfo<SimulationQueueEntry> {

    private static final Log LOG = LogFactory.getLog(SimulationRuntimeInfo)

    private Simulation simulation
    private Integer priority
    private Date offeredAt
    private UUID id
    private Integer progress
    private SimulationState simulationState
    private DateTime estimatedSimulationEnd
    private String username
    private List<Throwable> simulationErrors
    private long offeredNanoTime
    boolean deleted = false

    SimulationRuntimeInfo(UUID id) {
        this.id = Preconditions.checkNotNull(id)
    }

    SimulationRuntimeInfo(SimulationQueueEntry entry) {
        this(entry.id)
        apply(entry)
    }

    @Override
    String getUsername() {
        username
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

    UUID getId() {
        id
    }

    Integer getProgress() {
        progress
    }

    SimulationState getSimulationState() {
        simulationState
    }

    @Override
    DateTime getEstimatedEnd() {
        estimatedSimulationEnd
    }

    String getEstimatedTime() {
        DateTime start = simulation?.start
        if (start) {
            DateTime end = simulation.end ?: estimatedSimulationEnd
            if (end) {
                Period period = new Period(start, end, PeriodType.minutes());
                return "${period.minutes} min"
            }
        }
        return 'n/a'
    }

    List<Throwable> getSimulationErrors() {
        simulationErrors
    }

    boolean apply(SimulationQueueEntry entry) {
        boolean changed = false
        if (entry.id != id) {
            throw new IllegalStateException("queueEntry id is different from our id")
        }
        simulation = entry.context.simulationTask.simulation
        offeredNanoTime = entry.offeredNanoTime

        if (priority != entry.priority) {
            priority = entry.priority
            changed = true
        }
        if (offeredAt != entry.offeredAt) {
            offeredAt = entry.offeredAt
            changed = true
        }
        if (progress != entry.context.simulationTask.progress) {
            LOG.debug("progress changed from $progress to ${entry.context.simulationTask.progress}")
            progress = entry.context.simulationTask.progress
            changed = true
        }
        if (simulationState != entry.context.simulationTask.simulationState) {
            simulationState = entry.context.simulationTask.simulationState
            changed = true
        }
        if (estimatedSimulationEnd != entry.context.simulationTask.estimatedSimulationEnd) {
            estimatedSimulationEnd = entry.context.simulationTask.estimatedSimulationEnd
            changed = true
        }
        if (username != entry.context.username) {
            username = entry.context.username
            changed = true
        }
        if (simulationErrors != entry.context.simulationTask.simulationErrors) {
            simulationErrors = entry.context.simulationTask.simulationErrors
            changed = true
        }
        changed
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        SimulationRuntimeInfo that = (SimulationRuntimeInfo) o
        if (id != that.id) return false
        return true
    }

    int hashCode() {
        return id.hashCode()
    }

    @Override
    int compareTo(SimulationRuntimeInfo o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
        }
        return priority.compareTo(o.priority)
    }


}
