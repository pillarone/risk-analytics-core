package org.pillarone.riskanalytics.core.queue

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * holds runtime information about running, queued and finished simulations.
 */
abstract class AbstractRuntimeService<Q extends IQueueEntry, T extends IRuntimeInfo<Q>> {
    protected final List<T> finished = []
    protected final Map<UUID, T> queuedMap = [:]
    protected T running
    protected Q runningEntry
    protected final Object lock = new Object()
    protected Timer timer
    protected MyQueueListener queueListener
    @Delegate
    protected final RuntimeInfoEventSupport support = new RuntimeInfoEventSupport()

    @PostConstruct
    void initialize() {
        queueListener = new MyQueueListener()
        queueService.addQueueListener(queueListener)
        postConstruct()
    }


    @PreDestroy
    void destroy() {
        preDestroy()
        queueService.removeQueueListener(queueListener)
        queueListener = null
    }

    abstract void postConstruct()

    abstract void preDestroy()

    abstract IQueueService<Q> getQueueService()

    List<T> getQueued() {
        synchronized (lock) {
            List<T> infos = new ArrayList<T>(queuedMap.values())
            if (running) {
                infos.add(running)
            }
            infos
        }
    }

    List<T> getFinished() {
        synchronized (lock) {
            new ArrayList<T>(finished)
        }
    }

    abstract T createRuntimeInfo(Q queueEntry)

    void startTimer() {
        if (timer) {
            timer.cancel()
        }
        timer = new Timer()
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running != null) {
                    if (running.apply(runningEntry)) {
                        AbstractRuntimeService.log.debug("applying $runningEntry to $running")
                        changed(running)
                    }
                }
            }
        }, 1000, 1000);
    }

    protected class MyQueueListener implements QueueListener<Q> {
        @Override
        void starting(Q entry) {
            synchronized (lock) {
                if (running) {
                    throw new IllegalStateException("starting called, but there is already a running simulation $running.id")
                }
                running = queuedMap.remove(entry.id)
                runningEntry = entry
                if (!running) {
                    throw new IllegalStateException("no info found for id: ${entry.id}")
                }
                support.starting(running)
                startTimer()
            }
        }

        @Override
        void removed(UUID id) {
            T info = queuedMap.remove(id)
            if (!info) {
                throw new IllegalStateException("no info found for id $id")
            }
            finished.add(info)
            removed(info)
        }

        @Override
        void finished(UUID id) {
            synchronized (lock) {
                if (!running || running.id != id) {
                    throw new IllegalStateException("finished was called, but there is a different task running")
                }
                stopTimer()
                running.apply(runningEntry)
                finished.add(running)
                runningEntry = null
                T reference = running
                running = null
                finished(reference)
            }
        }

        @Override
        void offered(Q entry) {
            synchronized (lock) {
                T info = createRuntimeInfo(entry)
                queuedMap[entry.id] = info
                support.offered(info)
            }
        }

        private stopTimer() {
            timer?.cancel()
            timer = null
        }
    }
}
