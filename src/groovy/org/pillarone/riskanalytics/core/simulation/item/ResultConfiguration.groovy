package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.apache.commons.lang.builder.HashCodeBuilder
import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.output.*
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

class ResultConfiguration extends ModellingItem {

    String comment
    VersionNumber versionNumber
    VersionNumber modelVersionNumber
    List<PacketCollector> collectors

    public ResultConfiguration(String name) {
        super(name)
        versionNumber = new VersionNumber("1")
        collectors = []
    }

    public ResultConfiguration(String name, Class modelClass) {
        this(name)
        this.modelClass = modelClass
    }

    public ResultConfiguration(ConfigObject configObject, String name) {
        this(configObject.containsKey("displayName") ? configObject.displayName : name)
        modelClass = configObject.model
        ConfigObject flatConfigObject = configObject.components.flatten()
        for (Map.Entry entry in flatConfigObject.entrySet()) {
            String newPath = entry.key.replace(".", ":")
            newPath = "${modelClass.simpleName - "Model"}:$newPath"
            collectors << new PacketCollector(path: newPath, mode: CollectingModeFactory.getStrategy(entry.value))
        }
    }

    protected ResultConfigurationDAO createDao() {
        return getDaoClass().newInstance()
    }

    @CompileStatic
    public Object getDaoClass() {
        return ResultConfigurationDAO
    }

    protected ResultConfigurationDAO loadFromDB() {
        return ResultConfigurationDAO.find(name, modelClass.name, versionNumber.toString())
    }

    /**
     * Returns ready-for-simulation collectors, which means that the wildcards of dynamic
     * components are replaced with the actual sub component names.
     * This is in contrast to getCollectors, which returns the collectors for UI use.
     */
    @CompileStatic
    public List<PacketCollector> getResolvedCollectors(Model model, CollectorFactory collectorFactory) {
        return collectorFactory.createCollectors(this, model)
    }

    protected void mapFromDao(Object dao, boolean completeLoad) {
        dao = dao as ResultConfigurationDAO
        name = dao.name
        modelClass = ModelRegistry.instance.getModelClass(dao.modelClassName)
        if (dao.model != null) {
            modelVersionNumber = new VersionNumber(dao.model.itemVersion)
        }
        comment = dao.comment
        versionNumber = new VersionNumber(dao.itemVersion)
        creationDate = dao.creationDate
        modificationDate = dao.modificationDate
        creator = dao.getCreator()
        lastUpdater = dao.getLastUpdater()

        //These collectors are used by the UI only, therefore wildcard collectors must not be resolved here
        collectors = dao.collectorInformation.collect { CollectorInformation ci ->
            PacketCollector collector = new PacketCollector(CollectingModeFactory.getStrategy(ci.collectingStrategyIdentifier))
            collector.path = ci.path.pathName
            return collector
        }
    }

    public void setCollector(String path, ICollectingModeStrategy mode) {
        PacketCollector collector = collectors.find { PacketCollector c ->
            c.path == path
        }
        if (collector != null) {
            if (mode) {
                collector.mode = mode
            } else {
                collectors.remove(collector)
            }
        } else {
            collectors << new PacketCollector(path: path, mode: mode)
        }
    }

    public PacketCollector getCollector(String path) {
        collectors.find { it.path == path }
    }

    protected void mapToDao(Object dao) {
        dao = dao as ResultConfigurationDAO
        dao.name = name
        dao.modelClassName = modelClass.getName()
        if (modelVersionNumber != null) {
            dao.model = ModelDAO.findByModelClassNameAndItemVersion(modelClass.name, modelVersionNumber.toString())
        }
        dao.comment = comment
        dao.itemVersion = versionNumber.toString()
        dao.creationDate = creationDate
        dao.modificationDate = modificationDate
        dao.creator = creator
        dao.lastUpdater = lastUpdater

        Collection<CollectorInformation> currentCollectors = dao.collectorInformation

        List<PathMapping> pathCache = PathMapping.list()
        for (PacketCollector collector in collectors) {
            CollectorInformation existingInformation = dao.collectorInformation.find { CollectorInformation info ->
                info.path.pathName == collector.path
            }
            if (existingInformation) {
                existingInformation.collectingStrategyIdentifier = collector.mode.identifier
            } else {
                def information = new CollectorInformation(
                        path: getPathMapping(pathCache, collector.path),
                        collectingStrategyIdentifier: collector.mode.identifier,
                        configuration: dao
                )
                if (dao.collectorInformation == null) {
                    dao.collectorInformation = []
                }
                dao.collectorInformation << information
            }
        }

        pathCache.clear()
        //Clone list to prevent ConcurrentModificationException
        for (CollectorInformation info in currentCollectors?.toList()?.clone()) {
            if (!collectors*.path.contains(info.path.pathName)) {
                dao.removeFromCollectorInformation(info)
                info.delete()
            }
        }

    }

    @Override
    protected Object deleteDaoImpl(Object dao) {
        SimulationProfileDAO.findAllByTemplate(dao as ResultConfigurationDAO).each {
            it.delete(flush: true)
        }
        return super.deleteDaoImpl(dao)
    }

    @CompileStatic
    void setModelClass(Class clazz) {
        super.setModelClass(clazz)
        modelVersionNumber = clazz ? Model.getModelVersion(clazz) : null
    }

    public boolean isUsedInSimulation() {
        return SimulationRun.find("from ${SimulationRun.class.name} as run where run.resultConfiguration.name = ? and run.resultConfiguration.modelClassName = ? and run.resultConfiguration.itemVersion =?", [name, modelClass.name, versionNumber.toString()]) != null
    }

    @CompileStatic
    boolean isEditable() {
        return !isUsedInSimulation()
    }

    public List<SimulationRun> getSimulations() {
        if (!isLoaded()) {
            load()
        }
        return SimulationRun.findAllByResultConfigurationAndToBeDeleted(dao, false)
    }


    ConfigObject toConfigObject() {
        if (!isLoaded()) {
            load()
        }

        ConfigObject original = new ConfigObject()
        original.model = getModelClass()
        original.displayName = name
        collectors.sort { it.path }.each { PacketCollector p ->
            ConfigObject configObject = original
            String simpleClassName = getModelClass().simpleName
            String correctedPath = p.path - "${simpleClassName.substring(0, simpleClassName.length() - 5)}:"

            String[] keys = "components:${correctedPath}".split(":")
            keys.eachWithIndex { key, index ->
                if (index + 1 == keys.length) {
                    configObject[key] = p.mode.identifier
                    return
                }
                configObject = configObject[key]
            }
        }

        return original
    }

    @CompileStatic
    IConfigObjectWriter getWriter() {
        return new ResultConfigurationWriter()
    }

    private PathMapping getPathMapping(List<PathMapping> cache, String path) {
        PathMapping mapping = cache.find { it.pathName == path }
        if (mapping != null) {
            return mapping

        }
        mapping = PathMapping.findByPathName(path)
        if (!mapping) {
            mapping = new PathMapping(pathName: path)
            if (!mapping.save()) {
                throw new RuntimeException("Cannot save path mapping: $path")
            }
        }
        return mapping
    }

    @Override
    String getNameAndVersion() {
        "$name v${versionNumber.toString()}"
    }

    public boolean equals(Object obj) {
        if (obj instanceof ResultConfiguration) {
            return super.equals(obj) && obj.versionNumber.equals(versionNumber)
        } else {
            return false
        }
    }

    @CompileStatic
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder()
        hashCodeBuilder.append(name)
        hashCodeBuilder.append(modelClass)
        hashCodeBuilder.append(versionNumber.toString())
        return hashCodeBuilder.toHashCode()
    }
}
