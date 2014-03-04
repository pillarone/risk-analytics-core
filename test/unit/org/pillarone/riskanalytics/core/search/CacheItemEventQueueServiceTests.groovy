package org.pillarone.riskanalytics.core.search

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.CacheItemHibernateListener
import org.pillarone.riskanalytics.core.modellingitem.ResourceCacheItem

import static org.pillarone.riskanalytics.core.search.CacheItemEventQueueService.*
import static org.pillarone.riskanalytics.core.search.CacheItemEventQueueService.CacheItemEvent.EventType.*

@TestFor(CacheItemEventQueueService)
class CacheItemEventQueueServiceTests {

    CacheItemEventQueueService cacheItemEventQueueService
    TestHibernateListener hibernateListener
    CacheItemEventConsumer consumer

    private ParameterizationDAO dao

    @Before
    void setUp() {
        hibernateListener = new TestHibernateListener()
        cacheItemEventQueueService = new CacheItemEventQueueService(cacheItemListener: hibernateListener)
        cacheItemEventQueueService.init()
        consumer = new CacheItemEventConsumer(new Object(), new Object())
        cacheItemEventQueueService.register(consumer)
        assert hibernateListener._listeners.size() == 1

    }

    @Test
    void testAddEvent() {
        CacheItem item = createCacheItem()
        hibernateListener.itemAdded(item)
        List<CacheItemEvent> events = cacheItemEventQueueService.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == ADDED
        assert first.item == item
    }

    @Test
    void testRemoveEvent() {
        CacheItem item = createCacheItem()
        hibernateListener.itemDeleted(item)
        List<CacheItemEvent> events = cacheItemEventQueueService.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == REMOVED
        assert first.item == item
    }

    @Test
    void testUpdateEvent() {
        CacheItem item = createCacheItem()
        hibernateListener.itemChanged(item)
        List<CacheItemEvent> events = cacheItemEventQueueService.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == UPDATED
        assert first.item == item
    }

    @Test(expected = NullPointerException)
    void testCleanup() {
        cacheItemEventQueueService.cleanUp()
        assert hibernateListener._listeners.size() == 0
        cacheItemEventQueueService.pollCacheItemEvents(consumer)
    }

    private ResourceCacheItem createCacheItem() {
        new ResourceCacheItem(1l, 'resource', null, null, null, null, null, null, null, false, null)
    }

    private static class TestHibernateListener extends CacheItemHibernateListener {
        void itemAdded(CacheItem item) {
            _listeners.each { it.itemAdded(item) }
        }

        void itemChanged(CacheItem item) {
            _listeners.each { it.itemChanged(item) }
        }

        void itemDeleted(CacheItem item) {
            _listeners.each { it.itemDeleted(item) }
        }
    }
}
