package org.pillarone.riskanalytics.core.modellingitem

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

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
            simulation.parameterization = getModellingItem(dao.parameterization)
            simulation.template = getModellingItem(dao.resultConfiguration)
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



}
