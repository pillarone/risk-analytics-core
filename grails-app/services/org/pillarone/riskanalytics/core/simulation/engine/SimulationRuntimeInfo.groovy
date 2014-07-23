package org.pillarone.riskanalytics.core.simulation.engine

import com.google.common.base.Preconditions
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.user.Person

class SimulationRuntimeInfo implements Comparable<SimulationRuntimeInfo> {

    private static final Log LOG = LogFactory.getLog(SimulationRuntimeInfo)

    private Simulation simulation
    private Integer priority
    private Date offeredAt
    private UUID id
    private Integer progress
    private SimulationState simulationState
    private DateTime estimatedSimulationEnd
    private Person offeredBy
    private List<Throwable> simulationErrors
    private long offeredNanoTime

    SimulationRuntimeInfo(UUID id) {
        this.id = Preconditions.checkNotNull(id)
    }

    SimulationRuntimeInfo(QueueEntry entry) {
        this(entry.id)
        apply(entry)
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

    DateTime getEstimatedSimulationEnd() {
        estimatedSimulationEnd
    }

    Person getOfferedBy() {
        offeredBy
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

    boolean apply(QueueEntry entry) {
        boolean changed = false
        if (entry.id != id) {
            throw new IllegalStateException("queueEntry id is different from our id")
        }
        simulation = entry.simulationTask.simulation
        offeredNanoTime = entry.offeredNanoTime

        if (priority != entry.priority) {
            priority = entry.priority
            changed = true
        }
        if (offeredAt != entry.offeredAt) {
            offeredAt = entry.offeredAt
            changed = true
        }
        if (progress != entry.simulationTask.progress) {
            LOG.debug("progress changed from $progress to ${entry.simulationTask.progress}")
            progress = entry.simulationTask.progress
            changed = true
        }
        if (simulationState != entry.simulationTask.simulationState) {
            simulationState = entry.simulationTask.simulationState
            changed = true
        }
        if (estimatedSimulationEnd != entry.simulationTask.estimatedSimulationEnd) {
            estimatedSimulationEnd = entry.simulationTask.estimatedSimulationEnd
            changed = true
        }
        if (offeredBy != entry.offeredBy) {
            offeredBy = entry.offeredBy
            changed = true
        }
        if (simulationErrors != entry.simulationTask.simulationErrors) {
            simulationErrors = entry.simulationTask.simulationErrors
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
