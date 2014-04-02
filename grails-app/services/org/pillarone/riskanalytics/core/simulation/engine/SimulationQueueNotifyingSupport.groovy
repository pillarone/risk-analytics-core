package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

@CompileStatic
class SimulationQueueNotifyingSupport {
    private static final Log LOG = LogFactory.getLog(SimulationQueueNotifyingSupport)
    private final List<ISimulationQueueListener> listeners = new ArrayList<ISimulationQueueListener>()

    void addSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (listeners) {
            listeners.add(listener)
        }
    }

    void removeSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (listeners) {
            listeners.remove(listener)
        }
    }

    void notifyStarting(QueueEntry queueEntry) {
        synchronized (listeners) {
            listeners.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.starting(queueEntry)
                }
            }
        }
    }

    void notifyRemoved(UUID id) {
        synchronized (listeners) {
            listeners.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.removed(id)
                }
            }
        }
    }

    void notifyFinished(UUID id) {
        synchronized (listeners) {
            listeners.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.finished(id)
                }
            }
        }
    }

    void notifyOffered(QueueEntry queueEntry) {
        synchronized (listeners) {
            listeners.each { ISimulationQueueListener listener ->
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
