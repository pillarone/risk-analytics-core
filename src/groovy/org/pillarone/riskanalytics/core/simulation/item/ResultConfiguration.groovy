package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.output.*
import org.pillarone.riskanalytics.core.model.Model

class ResultConfiguration extends ModellingItem {

    String comment
    VersionNumber versionNumber
    Date creationDate
    Date modificationDate
    List<PacketCollector> collectors

    public ResultConfiguration(String name) {
        super(name)
        versionNumber = new VersionNumber("1")
        collectors = []
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

    public Object getDaoClass() {
        return ResultConfigurationDAO
    }

    protected ResultConfigurationDAO loadFromDB() {
        def criteria = ResultConfigurationDAO.createCriteria()
        def results = criteria.list {
            eq('name', name)
            eq('itemVersion', versionNumber.toString())
            if (getModelClass() != null)
                eq('modelClassName', getModelClass().name)
        }
        return results.size() > 0 ? results.get(0) : null
    }

    /**
     * Returns ready-for-simulation collectors, which means that the wildcards of dynamic
     * components are replaced with the actual sub component names.
     * This is in contrast to getCollectors, which returns the collectors for UI use.
     */
    public List<PacketCollector> getResolvedCollectors(Model model, CollectorFactory collectorFactory) {
        if (dao) {
            return collectorFactory.createCollectors(dao, model)
        }
        return null
    }

    protected void mapFromDao(Object dao) {
        dao = dao as ResultConfigurationDAO
        name = dao.name
        modelClass = getClass().getClassLoader().loadClass(dao.modelClassName)
        comment = dao.comment
        versionNumber = new VersionNumber(dao.itemVersion)
        creationDate = dao.creationDate
        modificationDate = dao.modificationDate

        //These collectors are used by the UI only, therefore wildcard collectors must not be resolved here
        collectors = dao.collectorInformation.collect {CollectorInformation ci ->
            new PacketCollector(
                    path: ci.path.pathName,
                    mode: CollectingModeFactory.getStrategy(ci.collectingStrategyIdentifier)
            )
        }
    }

    protected void mapToDao(Object dao) {
        dao = dao as ResultConfigurationDAO
        dao.name = name
        dao.modelClassName = modelClass.getName()
        dao.comment = comment
        dao.itemVersion = versionNumber.toString()
        dao.creationDate = creationDate
        dao.modificationDate = modificationDate

        Collection<CollectorInformation> currentCollectors = dao.collectorInformation

        for (PacketCollector collector in collectors) {
            CollectorInformation existingInformation = dao.collectorInformation.find {CollectorInformation info ->
                info.path.pathName == collector.path
            }
            if (existingInformation) {
                existingInformation.collectingStrategyIdentifier = collector.mode.getIdentifier()
            } else {
                dao.addToCollectorInformation(new CollectorInformation(
                        path: getPathMapping(collector.path),
                        collectingStrategyIdentifier: collector.mode.getIdentifier()
                ))
            }
        }

        //Clone list to prevent ConcurrentModificationException
        for (CollectorInformation info in currentCollectors?.toList()?.clone()) {
            if (!collectors*.path.contains(info.path.pathName)) {
                dao.removeFromCollectorInformation(info)
                info.delete()
            }
        }

    }

    public boolean isUsedInSimulation() {
        if (!isLoaded()) {
            load()
        }
        return SimulationRun.findByResultConfigurationAndToBeDeleted(dao, false) != null
    }

    ConfigObject toConfigObject() {
        if (!isLoaded()) {
            load()
        }

        ConfigObject original = new ConfigObject()
        original.model = getModelClass()
        original.displayName = name
        collectors.sort { it.path }.each {PacketCollector p ->
            ConfigObject configObject = original
            String simpleClassName = getModelClass().simpleName
            String correctedPath = p.path - "${simpleClassName.substring(0, simpleClassName.length() - 5)}:"

            String[] keys = "components:${correctedPath}".split(":")
            keys.eachWithIndex {key, index ->
                if (index + 1 == keys.length) {
                    configObject[key] = p.mode.identifier
                    return
                }
                configObject = configObject[key]
            }
        }

        return original
    }

    IConfigObjectWriter getWriter() {
        return new ResultConfigurationWriter()
    }

    private PathMapping getPathMapping(String path) {
        PathMapping mapping = PathMapping.findByPathName(path)
        if (!mapping) {
            mapping = new PathMapping(pathName: path)
            if (!mapping.save()) {
                throw new RuntimeException("Cannot save path mapping: $path")
            }
        }
        return mapping
    }

}
