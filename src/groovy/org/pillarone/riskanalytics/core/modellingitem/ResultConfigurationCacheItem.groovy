package org.pillarone.riskanalytics.core.modellingitem

import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person

@CompileStatic
class ResultConfigurationCacheItem extends CacheItem {
    final VersionNumber versionNumber

    ResultConfigurationCacheItem(
            Long id,
            String
            name,
            Class modelClass,
            VersionNumber versionNumber,
            DateTime creationDate,
            DateTime modificationDate,
            Person creator,
            Person lastUpdater
    ) {
        super(id, creationDate, modificationDate, creator, lastUpdater, name, modelClass)
        this.versionNumber = versionNumber
    }

    ResultConfigurationCacheItem(Long id, String name, Class modelClass, VersionNumber versionNumber) {
        this(id, name, modelClass, versionNumber, null, null, null, null)
    }

    @Override
    Class getItemClass() {
        ResultConfiguration
    }
}
