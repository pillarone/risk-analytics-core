package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.queue.IQueueEntry
import org.pillarone.riskanalytics.core.queue.QueueListener

import java.util.concurrent.CopyOnWriteArraySet

@CompileStatic
class QueueNotifyingSupport<T extends IQueueEntry> {
    private static final Log LOG = LogFactory.getLog(QueueNotifyingSupport)
    private final Set<QueueListener> listeners = new CopyOnWriteArraySet<QueueListener>()

    void addQueueListener(QueueListener listener) {
        synchronized (listeners) {
            listeners.add(listener)
        }
    }

    void removeQueueListener(QueueListener listener) {
        synchronized (listeners) {
            listeners.remove(listener)
        }
    }

    void notifyStarting(T queueEntry) {
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

    void notifyOffered(T queueEntry) {
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
