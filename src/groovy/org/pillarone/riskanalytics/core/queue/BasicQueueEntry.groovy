package org.pillarone.riskanalytics.core.queue

class BasicQueueEntry<K> implements IQueueEntry<K> {
    final UUID id
    final Date offeredAt
    int priority
    final long offeredNanoTime
    private final IQueueTaskContext<K> context

    BasicQueueEntry(IQueueTaskContext<K> context, int priority) {
        this.priority = priority
        this.context = context
        id = UUID.randomUUID()
        offeredAt = new Date()
        offeredNanoTime = System.nanoTime()
    }

    BasicQueueEntry(UUID id) {
        this.context = null
        this.id = id
        this.priority = 0
        offeredAt = null
        offeredNanoTime = System.nanoTime()
    }

    IQueueTaskContext<K> getContext() {
        return context
    }

    int compareTo(IQueueEntry<K> o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
        }
        return priority.compareTo(o.priority)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BasicQueueEntry that = (BasicQueueEntry) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
