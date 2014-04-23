package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.item.Batch
import org.pillarone.riskanalytics.core.user.Person

class BatchCacheItem extends CacheItem {
    final String comment
    final boolean executed
    final String simulationProfileName
    ImmutableList<ParameterizationCacheItem> parameterizations

    BatchCacheItem(Long id, DateTime creationDate, DateTime modificationDate, Person creator, Person lastUpdater, String name, String comment, boolean executed, ImmutableList<ParameterizationCacheItem> parameterizations, String simulationProfileName) {
        super(id, creationDate, modificationDate, creator, lastUpdater, name, null)
        this.comment = comment
        this.executed = executed
        this.parameterizations = parameterizations
        this.simulationProfileName = simulationProfileName
    }

    @Override
    Class getItemClass() {
        Batch
    }
}
