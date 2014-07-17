package org.pillarone.riskanalytics.core.simulation.engine
import org.gridgain.grid.Grid
import org.gridgain.grid.GridTaskFuture
import org.gridgain.grid.typedef.CI1
import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import static com.google.common.base.Preconditions.checkNotNull

class SimulationQueueService {
    Grid grid

    private final PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>()
    private final Object lock = new Object()
    private CurrentTask currentTask
    private TaskListener taskListener
    private boolean busy = false
    private Timer pollingTimer

    @Delegate
    private SimulationQueueNotifyingSupport support = new SimulationQueueNotifyingSupport()

    @PostConstruct
    private void initialize() {
        taskListener = new TaskListener()
        pollingTimer = new Timer()
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            void run() {
                poll()
            }
        }, 1000, 500)
    }

    @PreDestroy
    void stopPollingTimer() {
        pollingTimer.cancel()
        pollingTimer = null
        taskListener = null
    }

    void offer(SimulationConfiguration configuration, int priority = 10) {
        preConditionCheck(configuration)
        synchronized (lock) {
            QueueEntry queueEntry = new QueueEntry(configuration, priority, currentUser)
            configuration.username = currentUser?.username
            queue.offer(queueEntry)
            notifyOffered(queueEntry)
        }
    }

    void cancel(UUID uuid) {
        checkNotNull(uuid)
        synchronized (lock) {
            if (currentTask?.entry?.id == uuid) {
                currentTask.entry.simulationTask.cancel()
                GridTaskFuture future = currentTask.gridTaskFuture
                taskListener.apply(currentTask.gridTaskFuture)
                future.cancel()
                return
            }
            if (queue.remove(new QueueEntry(uuid))) {
                notifyRemoved(uuid)
            }
        }
    }

    List<QueueEntry> getQueueEntries() {
        synchronized (lock) {
            queue.toArray().toList() as List<QueueEntry>
        }
    }

    // PMO-2797 - need ability to check if item (to open) is used in any queued sim, including currently running one
    //
    List<QueueEntry> getQueueEntriesIncludingCurrentTask() {
        synchronized (lock) {
            List<QueueEntry> allEntries = queue.toArray().toList() as List<QueueEntry>
            if (currentTask) {
                allEntries.add(0, currentTask.entry) // PMO-2797 want current task checked first (in case: almost done)
            }
            allEntries
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
                    notifyStarting(queueEntry)
                    GridTaskFuture future = grid.execute(queueEntry.simulationTask, queueEntry.simulationTask.simulationConfiguration)
                    future.listenAsync(taskListener)
                    currentTask = new CurrentTask(gridTaskFuture: future, entry: queueEntry)
                }
            }
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
            SimulationState simulationState = entry.simulationTask.simulationState
            switch (simulationState) {
                case SimulationState.FINISHED:
                case SimulationState.ERROR:
                case SimulationState.CANCELED:
                    notifyFinished(entry.id)
                    break
                case SimulationState.NOT_RUNNING:
                case SimulationState.INITIALIZING:
                case SimulationState.RUNNING:
                case SimulationState.SAVING_RESULTS:
                case SimulationState.POST_SIMULATION_CALCULATIONS:
                default:
                    log.error("task $entry has finished, but state was $simulationState. This is likely to an internal gridgain error")
                    entry.simulationTask.simulationErrors.add(new Throwable("internal gridgain error"))
                    entry.simulationTask.simulationState = SimulationState.ERROR
                    notifyFinished(entry.id)
            }
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

