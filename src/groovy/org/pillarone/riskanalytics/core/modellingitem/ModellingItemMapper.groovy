package org.pillarone.riskanalytics.core.modellingitem

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.*

class ModellingItemMapper {
    static Parameterization getModellingItem(ParameterizationDAO detachedDao) {
        ParameterizationDAO.withNewSession {
            ParameterizationDAO dao = ParameterizationDAO.get(detachedDao.id) ?: detachedDao
            Parameterization parameterization = new Parameterization(dao.name, ModellingItemMapper.classLoader.loadClass(dao.modelClassName))
            parameterization.id = dao.id
            parameterization.versionNumber = new VersionNumber(dao.itemVersion)
            parameterization.creationDate = dao.creationDate
            parameterization.modificationDate = dao.modificationDate
            parameterization.creator = dao.creator
            parameterization.lastUpdater = dao.lastUpdater
            parameterization.tags = dao.tags*.tag
            parameterization.valid = dao.valid
            parameterization.status = dao.status
            parameterization.dealId = dao.dealId
            return parameterization
        }
    }

    static ResultConfiguration getModellingItem(ResultConfigurationDAO dao) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(dao.name)
        resultConfiguration.id = dao.id
        resultConfiguration.modelClass = ModellingItemMapper.classLoader.loadClass(dao.modelClassName)
        resultConfiguration.versionNumber = new VersionNumber(dao.itemVersion)
        resultConfiguration.creationDate = dao.creationDate
        resultConfiguration.modificationDate = dao.modificationDate
        resultConfiguration.creator = dao.creator
        resultConfiguration.lastUpdater = dao.lastUpdater

        return resultConfiguration
    }

    static Simulation getModellingItem(SimulationRun detachedDao) {
        SimulationRun.withNewSession {
            SimulationRun dao = SimulationRun.get(detachedDao.id) ?: detachedDao
            Simulation simulation = new Simulation(dao.name)
            simulation.id = dao.id
            if (!dao.toBeDeleted){
                // simulation runs that are to be deleted do not have p14n and resultConfigs anymore.
                simulation.parameterization = getModellingItem(dao.parameterization)
                simulation.template = getModellingItem(dao.resultConfiguration)
            }
            simulation.modelClass = ModellingItemMapper.classLoader.loadClass(dao.model)
            simulation.tags = dao.tags*.tag
            simulation.end = dao.endTime
            simulation.start = dao.startTime
            simulation.creationDate = dao.creationDate
            simulation.modificationDate = dao.modificationDate
            simulation.creator = dao.creator
            return simulation
        }
    }

    static Resource getModellingItem(ResourceDAO detachedDao) {
        ResourceDAO.withNewSession {
            ResourceDAO dao = ResourceDAO.get(detachedDao.id) ?: detachedDao
            Resource resource = new Resource(dao.name, ModellingItemMapper.classLoader.loadClass(dao.resourceClassName))
            resource.id = dao.id
            resource.versionNumber = new VersionNumber(dao.itemVersion)
            resource.creationDate = dao.creationDate
            resource.modificationDate = dao.modificationDate
            resource.creator = dao.creator
            resource.lastUpdater = dao.lastUpdater
            resource.tags = dao.tags*.tag
            resource.valid = dao.valid
            resource.status = dao.status
            return resource
        }
    }

    static ModellingItem getModellingItem(Object dao) {
        return null
    }

    static Parameterization newItemInstance(Parameterization item) {
        Parameterization parameterization = new Parameterization(item.name, item.modelClass)
        parameterization.id = item.id
        parameterization.versionNumber = item.versionNumber
        parameterization.creationDate = item.creationDate
        parameterization.modificationDate = item.modificationDate
        parameterization.creator = item.creator
        parameterization.lastUpdater = item.lastUpdater
        parameterization.tags = item.tags
        parameterization.valid = item.valid
        parameterization.status = item.status
        parameterization.dealId = item.dealId
        return parameterization
    }

    static ModellingItem newItemInstance(ModellingItem item) {
        throw new IllegalArgumentException("Not implemented. Item ${item?.class} cannot be instantiated.")
    }

    static Simulation newItemInstance(Simulation item) {
        Simulation simulation = new Simulation(item.name)
        simulation.id = item.id
        if (item.parameterization){
            simulation.parameterization = newItemInstance(item.parameterization)
        }
        if (item.template){
            simulation.template = newItemInstance(item.template)
        }
        simulation.modelClass = item.modelClass
        simulation.tags = item.tags
        simulation.end = item.end
        simulation.start = item.start
        simulation.creationDate = item.creationDate
        simulation.modificationDate = item.modificationDate
        simulation.creator = item.creator
        return simulation
    }

    static Resource newItemInstance(Resource item) {
        Resource resource = new Resource(item.name, item.modelClass)
        resource.id = item.id
        resource.versionNumber = item.versionNumber
        resource.creationDate = item.creationDate
        resource.modificationDate = item.modificationDate
        resource.creator = item.creator
        resource.lastUpdater = item.lastUpdater
        resource.tags = item.tags
        resource.valid = item.valid
        resource.status = item.status
        return resource
    }

    static ResultConfiguration newItemInstance(ResultConfiguration item) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(item.name)
        resultConfiguration.id = item.id
        resultConfiguration.modelClass = item.modelClass
        resultConfiguration.versionNumber = item.versionNumber
        resultConfiguration.creationDate = item.creationDate
        resultConfiguration.modificationDate = item.modificationDate
        resultConfiguration.creator = item.creator
        resultConfiguration.lastUpdater = item.lastUpdater

        return resultConfiguration
    }


}
