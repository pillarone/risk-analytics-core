package org.pillarone.riskanalytics.core.queue

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.util.concurrent.CopyOnWriteArraySet

class QueueNotifyingSupport<Q extends IQueueEntry> {
    private static final Log LOG = LogFactory.getLog(QueueNotifyingSupport)
    private final Set<QueueListener> listeners = new CopyOnWriteArraySet<QueueListener>()

    void addQueueListener(QueueListener<Q> listener) {
        synchronized (listeners) {
            listeners.add(listener)
        }
    }

    void removeQueueListener(QueueListener<Q> listener) {
        synchronized (listeners) {
            listeners.remove(listener)
        }
    }

    void notifyStarting(Q queueEntry) {
        synchronized (listeners) {
            listeners.each { QueueListener listener ->
                doExceptionSave {
                    listener.starting(queueEntry)
                }
            }
        }
    }

    void notifyRemoved(UUID id) {
        synchronized (listeners) {
            listeners.each { QueueListener listener ->
                doExceptionSave {
                    listener.removed(id)
                }
            }
        }
    }

    void notifyFinished(UUID id) {
        synchronized (listeners) {
            listeners.each { QueueListener listener ->
                doExceptionSave {
                    listener.finished(id)
                }
            }
        }
    }

    void notifyOffered(Q queueEntry) {
        synchronized (listeners) {
            listeners.each { QueueListener listener ->
                doExceptionSave {
                    listener.offered(queueEntry)
                }
            }
        }
    }

    private static void doExceptionSave(Closure closure) {
        try {
            closure.call()
        } catch (Throwable t) {
            LOG.error("failed to call method on ISimulationQueueListener", t)
        }
    }
}
