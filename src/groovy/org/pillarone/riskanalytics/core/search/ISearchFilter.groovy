package org.pillarone.riskanalytics.core.search

import org.pillarone.riskanalytics.core.modellingitem.CacheItem


public interface ISearchFilter {

    boolean accept(CacheItem item)

}