package org.pillarone.riskanalytics.core.search

import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.ParameterizationCacheItem

class StatusFilter extends AbstractMultiValueFilter {

    @Override
    boolean accept(CacheItem item) {
        if (!valueList.empty) {
            return internalAccept(item)
        }

        return true
    }

    boolean internalAccept(CacheItem item) {
        return false
    }

    boolean internalAccept(ParameterizationCacheItem item) {
        return valueList.contains(item.status?.toString())
    }
}
