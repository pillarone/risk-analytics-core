package org.pillarone.riskanalytics.core.modellingitem

import org.pillarone.riskanalytics.core.simulation.item.*

class ModellingItemCopyUtils {
    static Parameterization copyModellingItem(Parameterization source, Parameterization target, boolean withDependencies = true) {
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
        if (withDependencies) {
            target.tags = source.tags
        }
        target.valid = source.valid
        target.status = source.status
        target.dealId = source.dealId
        return target
    }

    static ResultConfiguration copyModellingItem(ResultConfiguration source, ResultConfiguration target, boolean withDependencies = true) {
        if (!source) {
            return null
        }
        target = target ?: new ResultConfiguration(source.name)
        target.name = source.name
        target.id = source.id
        target.modelClass = source.modelClass
        target.versionNumber = source.versionNumber?.clone()
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.lastUpdater = source.lastUpdater
        return target
    }

    static Simulation copyModellingItem(Simulation source, Simulation target, boolean withDependencies = true) {
        if (!source) {
            return null
        }
        target = target ?: new Simulation(source.name)
        target.name = source.name
        target.id = source.id
        if (withDependencies) {
            target.parameterization = updateModellingItem(source.parameterization, target.parameterization, withDependencies)
            target.template = updateModellingItem(source.template, target.template, withDependencies)
            target.tags = source.tags
        }
        target.modelClass = source.modelClass
        target.modelVersionNumber = source.modelVersionNumber?.clone()
        target.end = source.end
        target.start = source.start
        target.creationDate = source.creationDate
        target.modificationDate = source.modificationDate
        target.creator = source.creator
        target.numberOfIterations = source.numberOfIterations
        return target
    }

    static Resource copyModellingItem(Resource source, Resource target, boolean withDependencies = true) {
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
        if (withDependencies) {
            target.tags = source.tags
        }
        target.valid = source.valid
        target.status = source.status
        return target
    }

    static ModellingItem copyModellingItem(ModellingItem source, ModellingItem target, boolean withDependencies = true) {
        if (!source) {
            return null
        }
        return target
    }
}
