package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import groovy.transform.CompileStatic
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person

@CompileStatic
class SimulationCacheItem extends CacheItem {

    ParameterizationCacheItem parameterization
    ResultConfigurationCacheItem resultConfiguration
    final ImmutableList<Tag> tags
    final VersionNumber modelVersionNumber
    final DateTime end
    final DateTime start
    final int numberOfIterations
    Long batchId
    BatchCacheItem batch
    final int randomSeed


    SimulationCacheItem(
            Long id,
            String name,
            ParameterizationCacheItem parameterization,
            ResultConfigurationCacheItem resultConfiguration,
            ImmutableList<Tag> tags,
            Class modelClass,
            VersionNumber modelVersionNumber,
            DateTime end,
            DateTime start,
            DateTime creationDate,
            DateTime modificationDate,
            Person creator,
            int numberOfIterations,
            BatchCacheItem batch,
            Integer randomSeed       //Somehow a null is coming in here during a test
    ) {
        super(id, creationDate, modificationDate, creator, null, name, modelClass)
        this.parameterization = parameterization
        this.resultConfiguration = resultConfiguration
        this.tags = tags
        this.modelVersionNumber = modelVersionNumber
        this.end = end
        this.start = start
        this.numberOfIterations = numberOfIterations
        this.batch = batch
        this.randomSeed = randomSeed
    }

    // Don't like the look of this - partially constructed objects
    //
    SimulationCacheItem(Long id, String name, Class modelClass, VersionNumber modelVersionNumber) {
        this(id, name, null, null, null, modelClass, modelVersionNumber, null, null, null, null, null, -1, null, -1)
    }

    @Override
    Class getItemClass() {
        Simulation
    }
}
