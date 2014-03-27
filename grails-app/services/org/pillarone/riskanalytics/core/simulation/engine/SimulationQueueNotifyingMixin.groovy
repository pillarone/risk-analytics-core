package org.pillarone.riskanalytics.core.simulation.engine

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

@CompileStatic
class SimulationQueueNotifyingMixin {
    private static final Log LOG = LogFactory.getLog(SimulationQueueNotifyingMixin)
    private static final List<ISimulationQueueListener> LISTENERS = new ArrayList<ISimulationQueueListener>()

    static void addSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.add(listener)
        }
    }

    static void removeSimulationQueueListener(ISimulationQueueListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener)
        }
    }

    static void notifyStarting(QueueEntry queueEntry) {
        synchronized (LISTENERS) {
            LISTENERS.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.starting(queueEntry)
                }
            }
        }
    }

    static void notifyCanceled(UUID id) {
        synchronized (LISTENERS) {
            LISTENERS.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.canceled(id)
                }
            }
        }
    }

    static void notifyFinished(UUID id) {
        synchronized (LISTENERS) {
            LISTENERS.each { ISimulationQueueListener listener ->
                doExceptionSave {
                    listener.finished(id)
                }
            }
        }
    }

    static void notifyOffered(QueueEntry queueEntry) {
        synchronized (LISTENERS) {
            LISTENERS.each { ISimulationQueueListener listener ->
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
