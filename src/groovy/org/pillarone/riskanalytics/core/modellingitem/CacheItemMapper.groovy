package org.pillarone.riskanalytics.core.modellingitem

import com.google.common.collect.ImmutableList
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class CacheItemMapper {
    static ParameterizationCacheItem getModellingItem(ParameterizationDAO detachedDao, boolean forDeletion = false) {
        checkForId(detachedDao)
        //we have to reload the dao, because tags are fetched lazy. Otherwise we would get an LazyInitializationError
        ParameterizationDAO.withNewSession {
            ParameterizationDAO dao = ParameterizationDAO.get(detachedDao.id) ?: detachedDao

            def modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName)
            forDeletion ? new ParameterizationCacheItem(dao.id, dao.name, modelClass, new VersionNumber(dao.itemVersion)) :
                    new ParameterizationCacheItem(dao.id, new VersionNumber(dao.itemVersion),
                            dao.name,
                            modelClass,
                            dao.creationDate,
                            dao.modificationDate,
                            dao.creator,
                            dao.lastUpdater,
                            ImmutableList.copyOf(dao.tags*.tag ?: []),
                            dao.valid,
                            dao.status,
                            dao.dealId
                    )
        }
    }

    static ResultConfigurationCacheItem getModellingItem(ResultConfigurationDAO dao, boolean forDeletion = false) {
        checkForId(dao)
        //we can use the (possibly detached) dao, because all properties are fetched eagerly
        def modelClass = CacheItemMapper.classLoader.loadClass(dao.modelClassName)
        def versionNumber = new VersionNumber(dao.itemVersion)
        forDeletion ? new ResultConfigurationCacheItem(dao.id, dao.name, modelClass, versionNumber) :
                new ResultConfigurationCacheItem(
                        dao.id,
                        dao.name,
                        modelClass,
                        versionNumber,
                        dao.creationDate,
                        dao.modificationDate,
                        dao.creator,
                        dao.lastUpdater
                )
    }

    static SimulationCacheItem getModellingItem(SimulationRun detachedDao, boolean forDeletion = false) {
        checkForId(detachedDao)
        //we have to reload the dao, because tags are fetched lazy. Otherwise we would get an LazyInitializationError
        SimulationRun.withNewSession {
            SimulationRun dao = SimulationRun.get(detachedDao.id) ?: detachedDao
            def versionNumber = dao.usedModel?.itemVersion ? new VersionNumber(dao.usedModel.itemVersion) : null
            def modelClass = CacheItemMapper.classLoader.loadClass(dao.model)
            forDeletion ? new SimulationCacheItem(dao.id, dao.name, modelClass, versionNumber) :
                    new SimulationCacheItem(
                            dao.id,
                            dao.name,
                            !dao.toBeDeleted ? getModellingItem(dao.parameterization) : null,
                            !dao.toBeDeleted ? getModellingItem(dao.resultConfiguration) : null,
                            !dao.toBeDeleted ? ImmutableList.copyOf(dao.tags*.tag ?: []) : null,
                            modelClass,
                            versionNumber,
                            dao.endTime,
                            dao.startTime,
                            dao.creationDate,
                            dao.modificationDate,
                            dao.creator,
                            dao.iterations,
                            dao.batchRun?.id
                    )
        }
    }

    static ResourceCacheItem getModellingItem(ResourceDAO detachedDao, boolean forDeletion = false) {
        checkForId(detachedDao)
        //we have to reload the dao, because tags are fetched lazy. Otherwise we would get an LazyInitializationError
        ResourceDAO.withNewSession {
            ResourceDAO dao = ResourceDAO.get(detachedDao.id) ?: detachedDao
            def modelClass = CacheItemMapper.classLoader.loadClass(dao.resourceClassName)
            forDeletion ? new ResourceCacheItem(dao.id, dao.name, modelClass, new VersionNumber(dao.itemVersion)) :
                    new ResourceCacheItem(
                            dao.id,
                            dao.name,
                            modelClass,
                            new VersionNumber(dao.itemVersion),
                            dao.creationDate,
                            dao.modificationDate,
                            dao.creator,
                            dao.lastUpdater,
                            ImmutableList.copyOf(dao.tags*.tag ?: []),
                            dao.valid,
                            dao.status
                    )
        }
    }

    static BatchCacheItem getModellingItem(BatchRun dao, boolean forDeletion = false) {
        checkForId(dao)
        BatchRun.withNewSession {
            forDeletion ? new BatchCacheItem(dao.id, null, null, null, null, dao.name, null, dao.executed, null) :
                    new BatchCacheItem(
                            dao.id,
                            dao.creationDate,
                            dao.modificationDate,
                            dao.creator,
                            dao.lastUpdater,
                            dao.name,
                            dao.comment,
                            dao.executed,
                            ImmutableList.copyOf(dao.simulationRuns.collect { SimulationRun run -> getModellingItem(run, true) }),
                    )
        }
    }

    static CacheItem getModellingItem(Object dao, boolean forDeletion = false) {
        return null
    }

    private static checkForId(def detachedDao) {
        if (!detachedDao.id) {
            throw new IllegalStateException('dao not persistent yet. This is not allowed')
        }
    }

}
