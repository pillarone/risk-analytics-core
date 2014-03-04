package org.pillarone.riskanalytics.core.search

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.CacheItemHibernateListener
import org.pillarone.riskanalytics.core.modellingitem.CacheItemListener

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentHashMap

class CacheItemEventQueueService {

    static transactional = false
    private final static Log LOG = LogFactory.getLog(CacheItemEventQueueService)

    CacheItemHibernateListener cacheItemListener
    private Map<CacheItemEventConsumer, List<CacheItemEvent>> queue
    private CacheItemListener listener
    private final Object queueLock = new Object()

    @PostConstruct
    void init() {
        queue = new ConcurrentHashMap<CacheItemEventConsumer, List<CacheItemEvent>>()
        listener = new QueueCacheItemListener()
        cacheItemListener.addCacheItemListener(listener)
    }

    @PreDestroy
    void cleanUp() {
        cacheItemListener.removeCacheItemListener(listener)
        queue = null
    }

    public static CacheItemEventQueueService getInstance() {
        return Holders.grailsApplication.mainContext.getBean(CacheItemEventQueueService)
    }

    void register(CacheItemEventConsumer consumer) {
        if (queue.containsKey(consumer)) {
            LOG.warn("Consumer already registered $consumer")
        }
        queue[consumer] = new ArrayList<CacheItemEvent>()
    }

    void unregisterAllConsumersForSession(Object session) {
        synchronized (queueLock) {
            queue.keySet().findAll { CacheItemEventConsumer c -> c.session == session }.each { queue.remove(it) }
        }
    }

    List<CacheItemEvent> pollCacheItemEvents(CacheItemEventConsumer consumer) {
        synchronized (queueLock) {
            List<CacheItemEvent> result = queue[consumer]
            queue[consumer] = new ArrayList<CacheItemEvent>()
            return result
        }
    }

    private class QueueCacheItemListener implements CacheItemListener {
        void itemAdded(CacheItem item) {
            synchronized (queueLock) {
                for (List<CacheItemEvent> events in queue.values()) {
                    events << new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.ADDED)
                }
            }
        }

        void itemDeleted(CacheItem item) {
            synchronized (queueLock) {
                for (List<CacheItemEvent> events in queue.values()) {
                    events << new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.REMOVED)
                }
            }
        }

        void itemChanged(CacheItem item) {
            synchronized (queueLock) {
                for (List<CacheItemEvent> events in queue.values()) {
                    events << new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.UPDATED)
                }
            }
        }
    }

    public static class CacheItemEvent {

        CacheItem item
        EventType eventType

        @Override
        String toString() {
            return "$item $eventType"
        }

        enum EventType {
            ADDED, REMOVED, UPDATED
        }
    }
}
