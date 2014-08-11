package org.pillarone.riskanalytics.core.queue

import com.google.common.base.Preconditions

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

abstract class AbstractQueueService<K, Q extends IQueueEntry<K>> implements IQueueService<Q> {

    protected final PriorityQueue<Q> queue = new PriorityQueue<Q>()
    protected final Object lock = new Object()
    protected CurrentTask<Q> currentTask
    protected TaskListener taskListener
    protected boolean busy = false
    protected Timer pollingTimer

    @Delegate
    private QueueNotifyingSupport<Q> support = new QueueNotifyingSupport<Q>()

    @Override
    void removeQueueListener(QueueListener<Q> queueListener) {
        support.removeQueueListener(queueListener)
    }

    @Override
    void addQueueListener(QueueListener<Q> queueListener) {
        support.addQueueListener(queueListener)
    }

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

    void offer(K configuration, int priority = 5) {
        preConditionCheck(configuration)
        synchronized (lock) {
            Q queueEntry = createQueueEntry(configuration, priority)
            queue.offer(queueEntry)
            support.notifyOffered(queueEntry)
        }

    }

    abstract Q createQueueEntry(K configuration, int priority)

    abstract Q createQueueEntry(UUID id)

    abstract void preConditionCheck(K configuration)

    void cancel(UUID uuid) {
        Preconditions.checkNotNull(uuid)
        synchronized (lock) {
            if (currentTask?.entry?.id == uuid) {
                currentTask.future.cancel()
                return
            }
            if (queue.remove(createQueueEntry(uuid))) {
                support.notifyRemoved(uuid)
            }
        }
    }

    List<Q> getQueueEntries() {
        synchronized (lock) {
            queue.toArray().toList() as List<Q>
        }
    }

    List<Q> getQueueEntriesIncludingCurrentTask() {
        synchronized (lock) {
            List<Q> allEntries = queue.toArray().toList() as List<Q>
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
                    throw new IllegalStateException("Want to start new job. But there is still a running one")
                }
                Q queueEntry = queue.poll()
                if (queueEntry) {
                    busy = true
                    support.notifyStarting(queueEntry)
                    IQueueTaskFuture future = doWork(queueEntry, queueEntry.priority)
                    future.listenAsync(taskListener)
                    currentTask = new CurrentTask<Q>(future: future, entry: queueEntry)
                }
            }
        }
    }

    abstract IQueueTaskFuture doWork(Q entry, int priority)

    private void queueTaskFinished(IQueueTaskFuture future) {
        synchronized (lock) {
            if (!currentTask) {
                throw new IllegalStateException('simulation ended, but there is no currentTask')
            }
            busy = false
            Q entry = currentTask.entry
            currentTask = null
            future.stopListenAsync(taskListener)
            handleEntry(entry)
            support.notifyFinished(entry.id)
        }
    }

    abstract void handleEntry(Q entry)

    private static class CurrentTask<Q extends IQueueEntry> {
        IQueueTaskFuture future
        Q entry
    }

    private class TaskListener implements IQueueTaskListener {
        void apply(IQueueTaskFuture future) {
            queueTaskFinished(future)
        }
    }

}




