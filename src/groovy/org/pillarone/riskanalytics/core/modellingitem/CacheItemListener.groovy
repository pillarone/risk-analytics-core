package org.pillarone.riskanalytics.core.modellingitem

public interface CacheItemListener {
    void itemAdded(CacheItem item);

    void itemDeleted(CacheItem item);

    void itemChanged(CacheItem item);
}