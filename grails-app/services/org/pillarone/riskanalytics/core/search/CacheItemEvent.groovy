package org.pillarone.riskanalytics.core.search;

import org.pillarone.riskanalytics.core.modellingitem.CacheItem;

class CacheItemEvent {

    CacheItem item
    EventType eventType

    @Override
    String toString() {
        return "$item $eventType"
    }

    static enum EventType {
        ADDED, REMOVED, UPDATED
    }
}