package org.pillarone.riskanalytics.core.queue

class AbstractQueueEntry<C extends IQueueTaskContext> implements IQueueEntry<C> {
    final UUID id
    final Date offeredAt
    int priority
    final long offeredNanoTime
    final C context


    AbstractQueueEntry(C context, int priority) {
        this.priority = priority
        id = UUID.randomUUID()
        offeredAt = new Date()
        offeredNanoTime = System.nanoTime()
        this.context = context
    }

    AbstractQueueEntry(UUID id) {
        this.id = id
        this.priority = 0
        offeredAt = null
        offeredNanoTime = System.nanoTime()
        context = null
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
