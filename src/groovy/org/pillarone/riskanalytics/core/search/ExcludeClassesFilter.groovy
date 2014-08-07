package org.pillarone.riskanalytics.core.search

import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem

class ExcludeClassesFilter implements ISearchFilter {

    final List<? extends Class<ModellingItem>> classes

    ExcludeClassesFilter(List<? extends Class<ModellingItem>> classes) {
        this.classes = classes
    }

    @Override
    boolean accept(CacheItem item) {
        !classes.contains(item.itemClass)
    }
}
