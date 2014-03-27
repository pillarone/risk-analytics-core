package org.pillarone.riskanalytics.core.simulation.engine

import org.gridgain.grid.GridTaskFuture
import org.gridgain.grid.typedef.CI1
import org.pillarone.riskanalytics.core.simulation.engine.grid.SimulationHandler
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement

import javax.annotation.PostConstruct

import static com.google.common.base.Preconditions.checkNotNull

@Mixin(SimulationQueueNotifyingMixin)
class SimulationQueueService {
    SimulationStartService simulationStartService

    private final PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>()
    private final Object lock = new Object()
    private CurrentTask currentTask
    private TaskListener taskListener
    private boolean busy = false

    @PostConstruct
    private void initialize() {
        taskListener = new TaskListener()
    }

    SimulationHandler offer(SimulationConfiguration configuration, int priority = 10) {
        preConditionCheck(configuration)
        synchronized (lock) {
            QueueEntry queueEntry = new QueueEntry(configuration, priority, currentUser)
            queue.offer(queueEntry)
            notifyOffered(queueEntry)
            poll()
            new SimulationHandler(queueEntry.simulationTask, queueEntry.id)
        }
    }

    void cancel(UUID uuid) {
        checkNotNull(uuid)
        synchronized (lock) {
            def entry = new QueueEntry(uuid)
            queue.remove(entry)
            if (currentTask?.entry?.id == uuid) {
                currentTask.gridTaskFuture.cancel()
                //notifyCanceled is not necessary here. Instead notifyFinished will be called in taskListener
            } else {
                notifyCanceled(uuid)
            }
        }
    }

    List<QueueEntry> getQueueEntries() {
        synchronized (lock) {
            queue.toArray().toList() as List<QueueEntry>
        }
    }

    private void poll() {
        synchronized (lock) {
            if (!busy) {
                if (currentTask) {
                    throw new IllegalStateException("Want to start new simulation. But there is still a running one")
                }
                QueueEntry queueEntry = queue.poll()
                if (queueEntry) {
                    busy = true
                    simulationStartService.start(queueEntry) { GridTaskFuture future, QueueEntry entry ->
                        setCurrentTask(future, entry)
                    }
                    notifyStarting(queueEntry)
                }
            }
        }
    }

    private void setCurrentTask(GridTaskFuture future, QueueEntry entry) {
        synchronized (lock) {
            future.listenAsync(taskListener)
            currentTask = new CurrentTask(gridTaskFuture: future, entry: entry)
        }
    }

    private void gridTaskFinished(GridTaskFuture future) {
        synchronized (lock) {
            if (!currentTask) {
                throw new IllegalStateException('simulation ended, but there is no currentTask')
            }
            busy = false
            QueueEntry entry = currentTask.entry
            currentTask = null
            future.stopListenAsync(taskListener)
            notifyFinished(entry.id)
            poll()
        }
    }

    private static class CurrentTask {
        GridTaskFuture gridTaskFuture
        QueueEntry entry
    }

    private class TaskListener extends CI1<GridTaskFuture> {
        @Override
        void apply(GridTaskFuture future) {
            gridTaskFinished(future)
        }
    }

    private static Person getCurrentUser() {
        UserManagement.currentUser
    }

    private static void preConditionCheck(SimulationConfiguration configuration) {
        //TODO discuss, what has to be fulfilled
        Long id = configuration?.simulation?.id
        if (!id) {
            throw new IllegalStateException('simulation must be persistent before putting it on the queue')
        }
    }
}

