package org.pillarone.riskanalytics.core.queue


interface IRuntimeInfoListener<T> {
    void starting(T info)

    void finished(T info)

    void removed(T info)

    void offered(T info)

    void changed(T info)
}

