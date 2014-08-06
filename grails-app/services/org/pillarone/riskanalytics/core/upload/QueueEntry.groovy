package org.pillarone.riskanalytics.core.upload

class QueueEntry {
    final UUID id
    final Date offeredAt
    int priority
    final long offeredNanoTime
    final UploadConfiguration uploadConfiguration
    UploadResult uploadResult

    QueueEntry(UploadConfiguration uploadConfiguration, int priority) {
        this.uploadConfiguration = uploadConfiguration
        this.priority = priority
        id = UUID.randomUUID()
        offeredAt = new Date()
        offeredNanoTime = System.nanoTime()
    }

    QueueEntry(UUID id) {
        this.id = id
        this.priority = 0
        offeredAt = null
        offeredNanoTime = System.nanoTime()
        uploadConfiguration = null
    }

    int compareTo(QueueEntry o) {
        if (priority.equals(o.priority)) {
            return offeredNanoTime.compareTo(o.offeredNanoTime)
        }
        return priority.compareTo(o.priority)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        QueueEntry that = (QueueEntry) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
