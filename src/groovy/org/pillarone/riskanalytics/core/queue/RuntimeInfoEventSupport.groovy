package org.pillarone.riskanalytics.core.queue

import java.util.concurrent.CopyOnWriteArrayList

class RuntimeInfoEventSupport<T> {
    private
    final Set<IRuntimeInfoListener> infoListeners = new CopyOnWriteArrayList<IRuntimeInfoListener<T>>()

    void addRuntimeInfoListener(IRuntimeInfoListener listener) {
        synchronized (listener) {
            infoListeners << listener
        }
    }

    void removeRuntimeInfoListener(IRuntimeInfoListener listener) {
        synchronized (listener) {
            infoListeners.remove(listener)
        }
    }

    void offered(T info) {
        synchronized (infoListeners) {
            infoListeners.each { IRuntimeInfoListener listener -> listener.offered(info) }
        }
    }

    void starting(T info) {
        synchronized (infoListeners) {
            infoListeners.each { IRuntimeInfoListener listener -> listener.starting(info) }
        }
    }

    void finished(T info) {
        synchronized (infoListeners) {
            infoListeners.each { IRuntimeInfoListener listener -> listener.finished(info) }
        }
    }

    void removed(T info) {
        synchronized (infoListeners) {
            infoListeners.each { IRuntimeInfoListener listener -> listener.removed(info) }
        }
    }

    void changed(T info) {
        synchronized (infoListeners) {
            infoListeners.each { IRuntimeInfoListener listener -> listener.changed(info) }
        }
    }
}
