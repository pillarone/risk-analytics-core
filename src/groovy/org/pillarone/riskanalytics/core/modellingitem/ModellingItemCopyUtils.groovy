package org.pillarone.riskanalytics.core.modellingitem

import org.pillarone.riskanalytics.core.simulation.item.*

class ModellingItemCopyUtils {
    static Parameterization copyModellingItem(Parameterization source, boolean withDependencies = true) {
        Parameterization parameterization = new Parameterization(source.name, source.modelClass)
        parameterization.id = source.id
        parameterization.versionNumber = source.versionNumber?.clone()
        parameterization.creationDate = source.creationDate
        parameterization.modificationDate = source.modificationDate
        parameterization.creator = source.creator
        parameterization.lastUpdater = source.lastUpdater
        if (withDependencies) {
            parameterization.tags = source.tags
        }
        parameterization.valid = source.valid
        parameterization.status = source.status
        parameterization.dealId = source.dealId
        return parameterization
    }

    static ResultConfiguration copyModellingItem(ResultConfiguration dao, boolean withDependencies = true) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(dao.name)
        resultConfiguration.id = dao.id
        resultConfiguration.modelClass = dao.modelClass
        resultConfiguration.versionNumber = dao.versionNumber?.clone()
        resultConfiguration.creationDate = dao.creationDate
        resultConfiguration.modificationDate = dao.modificationDate
        resultConfiguration.creator = dao.creator
        resultConfiguration.lastUpdater = dao.lastUpdater
        return resultConfiguration
    }

    static Simulation copyModellingItem(Simulation dao, boolean withDependencies = true) {
        Simulation simulation = new Simulation(dao.name)
        simulation.id = dao.id
        if (withDependencies) {
            simulation.parameterization = copyModellingItem(dao.parameterization)
            simulation.template = copyModellingItem(dao.template)
            simulation.tags = dao.tags
        }
        simulation.modelClass = dao.modelClass
        simulation.modelVersionNumber = dao.modelVersionNumber?.clone()
        simulation.end = dao.end
        simulation.start = dao.start
        simulation.creationDate = dao.creationDate
        simulation.modificationDate = dao.modificationDate
        simulation.creator = dao.creator
        simulation.numberOfIterations = dao.numberOfIterations
        return simulation
    }

    static Resource copyModellingItem(Resource dao, boolean withDependencies = true) {
        Resource resource = new Resource(dao.name, dao.modelClass)
        resource.id = dao.id
        resource.versionNumber = dao.versionNumber?.clone()
        resource.creationDate = dao.creationDate
        resource.modificationDate = dao.modificationDate
        resource.creator = dao.creator
        resource.lastUpdater = dao.lastUpdater
        if (withDependencies) {
            resource.tags = dao.tags
        }
        resource.valid = dao.valid
        resource.status = dao.status
        return resource
    }

    static ModellingItem copyModellingItem(Object dao, boolean withDependencies = true) {
        return null
    }
}
