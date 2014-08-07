package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.simulation.SimulationState
import org.pillarone.riskanalytics.core.simulation.engine.QueueNotifyingSupport
import org.pillarone.riskanalytics.core.simulation.item.Simulation

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class UploadQueueService {

    IUploadStrategy uploadStrategy

    private final PriorityQueue<QueueEntry> queue = new PriorityQueue<QueueEntry>()
    private final Object lock = new Object()
    private CurrentTask currentTask
    private TaskListener taskListener
    private boolean busy = false
    private Timer pollingTimer

    @Delegate
    private QueueNotifyingSupport support = new QueueNotifyingSupport<QueueEntry>()

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

    void upload(UploadConfiguration configuration, int priority = 5) {
        preConditionCheck(configuration)
        synchronized (lock) {
            QueueEntry queueEntry = new QueueEntry(configuration, priority)
            queue.offer(queueEntry)
            notifyOffered(queueEntry)
        }

    }

    void cancel(UUID uuid) {
        checkNotNull(uuid)
        synchronized (lock) {
            if (currentTask?.entry?.id == uuid) {
                currentTask.uploadFuture.cancel()
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

    List<QueueEntry> getQueueEntriesIncludingCurrentTask() {
        synchronized (lock) {
            List<QueueEntry> allEntries = queue.toArray().toList() as List<QueueEntry>
            if (currentTask) {
                allEntries.add(0, currentTask.entry)
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
                    IUploadFuture future = uploadStrategy.upload(queueEntry.uploadConfiguration, queueEntry.priority)
                    future.listenAsync(taskListener)
                    currentTask = new CurrentTask(uploadFuture: future, entry: queueEntry)
                }
            }
        }
    }

    private void uploadTaskFinished(IUploadFuture future) {
        synchronized (lock) {
            if (!currentTask) {
                throw new IllegalStateException('simulation ended, but there is no currentTask')
            }
            busy = false
            QueueEntry entry = currentTask.entry
            currentTask = null
            future.stopListenAsync(taskListener)
            def result = future.uploadResult
            if (!result) {
                throw new IllegalStateException("upload task finished without result")
            }
            entry.uploadResult = result
            notifyFinished(entry.id)
        }
    }

    private static class CurrentTask {
        IUploadFuture uploadFuture
        QueueEntry entry
    }

    private class TaskListener implements IUploadTaskListener {
        void apply(IUploadFuture future) {
            uploadTaskFinished(future)
        }
    }

    private static void preConditionCheck(UploadConfiguration configuration) {
        checkNotNull(configuration)
        Simulation simulation = configuration.simulation
        checkNotNull(simulation)
        checkNotNull(simulation.id)
        checkNotNull(simulation.start)
        checkNotNull(simulation.end)
        checkNotNull(simulation.template)
        checkArgument(simulation.simulationState == SimulationState.FINISHED)
    }
}




