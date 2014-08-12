package org.pillarone.riskanalytics.core.modellingitem

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.item.SimulationProfile
import org.pillarone.riskanalytics.core.user.Person

class SimulationProfileCacheItem extends CacheItem {
    final Integer randomSeed
    final Integer numberOfIterations
    final boolean forPublic
    final ResultConfigurationCacheItem template


    SimulationProfileCacheItem(Long id, DateTime creationDate, DateTime modificationDate, Person creator, Person lastUpdater, String name, Class modelClass, Integer randomSeed, Integer numberOfIterations, boolean forPublic, ResultConfigurationCacheItem template) {
        super(id, creationDate, modificationDate, creator, lastUpdater, name, modelClass)
        this.randomSeed = randomSeed
        this.numberOfIterations = numberOfIterations
        this.forPublic = forPublic
        this.template = template
    }

    SimulationProfileCacheItem(Long id, String name, Class modelClass) {
        super(id, null, null, null, null, name, modelClass)
        randomSeed = null
        numberOfIterations = null
        forPublic = false
        template = null
    }

    @Override
    Class getItemClass() {
        SimulationProfile
    }
}
