package org.pillarone.riskanalytics.core.queue

class AbstractQueueEntry<R extends IResult> implements IQueueEntry<R> {
    final UUID id
    final Date offeredAt
    int priority
    final long offeredNanoTime
    final IConfiguration configuration
    R result

    AbstractQueueEntry(IConfiguration configuration, int priority) {
        this.configuration = configuration
        this.priority = priority
        id = UUID.randomUUID()
        offeredAt = new Date()
        offeredNanoTime = System.nanoTime()
    }

    AbstractQueueEntry(UUID id) {
        this.id = id
        this.priority = 0
        offeredAt = null
        offeredNanoTime = System.nanoTime()
        configuration = null
    }

    int compareTo(IQueueEntry o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
        }
        return priority.compareTo(o.priority)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        AbstractQueueEntry that = (AbstractQueueEntry) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
