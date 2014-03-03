package org.pillarone.riskanalytics.core.search

class CacheItemEventConsumer {
    final Object session
    final Object consumer

    CacheItemEventConsumer(Object session, Object consumer) {
        this.session = session
        this.consumer = consumer
    }

    @Override
    String toString() {
        "$consumer $session"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        CacheItemEventConsumer that = (CacheItemEventConsumer) o

        if (consumer != that.consumer) return false
        if (session != that.session) return false

        return true
    }

    int hashCode() {
        int result
        result = session.hashCode()
        result = 31 * result + consumer.hashCode()
        return result
    }
}
