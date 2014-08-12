package org.pillarone.riskanalytics.core.modellingitem

import org.pillarone.riskanalytics.core.simulation.item.*

class ModellingItemUpdater {
    static Parameterization createOrUpdateModellingItem(ParameterizationCacheItem source, Parameterization target) {
        if (!source) {
            return null
        }
        target = target ?: new Parameterization(source.name)
        target.name = source.name
        target.modelClass = source.modelClass
        target.id = source.id
        target.versionNumber = source.versionNumber?.clone()
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        target.tags = new ArrayList(source.tags ?: [])
        target.valid = source.valid
        target.status = source.status
        target.dealId = source.dealId
        return target
    }

    static ResultConfiguration createOrUpdateModellingItem(ResultConfigurationCacheItem source, ResultConfiguration target) {
        if (!source) {
            return null
        }
        target = target ?: new ResultConfiguration(source.name, source.modelClass)
        target.name = source.name
        target.id = source.id
        target.versionNumber = source.versionNumber?.clone()
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        return target
    }

    static Simulation createOrUpdateModellingItem(SimulationCacheItem source, Simulation target) {
        if (!source) {
            return null
        }
        target = target ?: new Simulation(source.name)
        target.name = source.name
        target.id = source.id
        target.parameterization = createOrUpdateModellingItem(source.parameterization, target.parameterization)
        target.template = createOrUpdateModellingItem(source.resultConfiguration, target.template)
        target.batch = createOrUpdateModellingItem(source.batch, target.batch)
        target.tags = new ArrayList(source.tags ?: [])
        target.modelClass = source.modelClass
        target.modelVersionNumber = source.modelVersionNumber?.clone()
        target.end = source.end
        target.start = source.start
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.numberOfIterations = source.numberOfIterations
        target.randomSeed = source.randomSeed
        return target
    }

    static SimulationProfile createOrUpdateModellingItem(SimulationProfileCacheItem source, SimulationProfile target) {
        if (!source) {
            return null
        }
        target = target ?: new SimulationProfile(source.name, source.modelClass)
        target.name = source.name
        target.id = source.id
        target.template = createOrUpdateModellingItem(source.template, target.template)
        target.modelClass = source.modelClass
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        target.numberOfIterations = source.numberOfIterations
        target.randomSeed = source.randomSeed
        target.forPublic = source.forPublic
        return target
    }

    static Resource createOrUpdateModellingItem(ResourceCacheItem source, Resource target) {
        if (!source) {
            return null
        }
        target = target ?: new Resource(source.name, source.modelClass)
        target.name = source.name
        target.modelClass = source.modelClass
        target.id = source.id
        target.versionNumber = source.versionNumber?.clone()
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        target.tags = new ArrayList<>(source.tags ?: [])
        target.valid = source.valid
        target.status = source.status
        return target
    }

    static Batch createOrUpdateModellingItem(BatchCacheItem source, Batch target) {
        if (!source) {
            return null
        }
        target = target ?: new Batch(source.name)
        target.name = source.name
        target.id = source.id
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        target.comment = source.comment
        target.executed = source.executed
        target.simulationProfileName = source.simulationProfileName
        target.parameterizations = source.parameterizations.collect { ParameterizationCacheItem parameterizationCacheItem ->
            Parameterization targetSimulation = target.parameterizations.find { Parameterization parameterization -> parameterization.id == parameterizationCacheItem.id }
            createOrUpdateModellingItem(parameterizationCacheItem, targetSimulation)
        }
        return target
    }

    static ModellingItem createOrUpdateModellingItem(CacheItem source, ModellingItem target) {
        if (!source) {
            return null
        }
        return target
    }
}
