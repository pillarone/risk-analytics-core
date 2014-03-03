package org.pillarone.riskanalytics.core.search

import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.ParameterizationCacheItem
import org.pillarone.riskanalytics.core.modellingitem.ResourceCacheItem
import org.pillarone.riskanalytics.core.modellingitem.SimulationCacheItem

class TagFilter extends AbstractMultiValueFilter {


    @Override
    boolean accept(CacheItem item) {
        if (!valueList.empty) {
            return internalAccept(item)
        }

        return true
    }

    protected boolean internalAccept(CacheItem item) {
        return false
    }

    protected boolean internalAccept(ParameterizationCacheItem item) {
        return item.tags*.name.any { valueList.contains(it) }
    }

    protected boolean internalAccept(SimulationCacheItem item) {
        return item.tags*.name.any { valueList.contains(it) }
    }

    protected boolean internalAccept(ResourceCacheItem item) {
        return item.tags*.name.any { valueList.contains(it) }
    }
}
