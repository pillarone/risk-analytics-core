package org.pillarone.riskanalytics.core.modellingitem

import org.hibernate.event.*

class CacheItemHibernateListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {
    final List<CacheItemListener> listeners = new ArrayList<CacheItemListener>();

    void addCacheItemListener(CacheItemListener listener) {
        assert !listeners.contains(listener);
        listeners.add(listener);
    }

    void removeCacheItemListener(CacheItemListener listener) {
        listeners.remove(listener);
    }

    void onPostInsert(PostInsertEvent postInsertEvent) {
        CacheItem item = CacheItemMapper.getModellingItem(postInsertEvent.entity)
        if (item != null) {
            for (CacheItemListener listener : listeners) {
                listener.itemAdded(item)
            }
        }
    }

    void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        def entity = postUpdateEvent.entity
        boolean toBeDeleted = isDeleted(entity)
        CacheItem item = CacheItemMapper.getModellingItem(entity, toBeDeleted)
        if (item != null) {
            for (CacheItemListener listener : listeners) {
                toBeDeleted ? listener.itemDeleted(item) : listener.itemChanged(item)
            }
        }
    }

    private boolean isDeleted(def entity) {
        entity.hasProperty('toBeDeleted') ? entity.toBeDeleted : false
    }

    void onPostDelete(PostDeleteEvent postDeleteEvent) {
        CacheItem item = CacheItemMapper.getModellingItem(postDeleteEvent.entity, true)
        if (item != null) {
            for (CacheItemListener listener : listeners) {
                listener.itemDeleted(item)
            }
        }
    }
}
