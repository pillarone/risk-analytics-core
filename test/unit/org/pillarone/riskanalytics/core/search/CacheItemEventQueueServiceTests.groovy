package org.pillarone.riskanalytics.core.search

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.CacheItemHibernateListener
import org.pillarone.riskanalytics.core.modellingitem.ResourceCacheItem

import static org.pillarone.riskanalytics.core.search.CacheItemEvent.EventType.*

@TestFor(CacheItemEventQueueService)
class CacheItemEventQueueServiceTests {

    TestHibernateListener hibernateListenerForTest
    CacheItemEventConsumer consumer

    @Before
    void setUp() {
        hibernateListenerForTest = new TestHibernateListener()
        service.cacheItemListener = hibernateListenerForTest
        service.init()
        consumer = new CacheItemEventConsumer(new Object(), new Object())
        service.register(consumer)
        assert hibernateListenerForTest._listeners.size() == 1

    }

    @Test
    void testAddEvent() {
        CacheItem item = createCacheItem()
        hibernateListenerForTest.itemAdded(item)
        List<CacheItemEvent> events = service.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == ADDED
        assert first.item == item
    }

    @Test
    void testRemoveEvent() {
        CacheItem item = createCacheItem()
        hibernateListenerForTest.itemDeleted(item)
        List<CacheItemEvent> events = service.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == REMOVED
        assert first.item == item
    }

    @Test
    void testUpdateEvent() {
        CacheItem item = createCacheItem()
        hibernateListenerForTest.itemChanged(item)
        List<CacheItemEvent> events = service.pollCacheItemEvents(consumer)
        assert events.size() == 1
        CacheItemEvent first = events.first()
        assert first.eventType == UPDATED
        assert first.item == item
    }

    @Test(expected = NullPointerException)
    void testCleanup() {
        service.cleanUp()
        assert hibernateListenerForTest._listeners.size() == 0
        service.pollCacheItemEvents(consumer)
    }

    private static ResourceCacheItem createCacheItem() {
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
