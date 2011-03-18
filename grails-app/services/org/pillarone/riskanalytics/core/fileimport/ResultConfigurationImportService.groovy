package org.pillarone.riskanalytics.core.fileimport

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.util.GroovyUtils

public class ResultConfigurationImportService extends FileImportService {

    private static final Log LOG = LogFactory.getLog(ResultConfigurationImportService)

    private ConfigObject configObject
    private String name

    public String getFileSuffix() {
        return "ResultConfiguration"
    }

    public getDaoClass() {
        return ResultConfigurationDAO
    }

    protected boolean saveItemObject(String fileContent) {
        List<PathMapping> pathCache = PathMapping.list()
        ResultConfiguration configuration = new ResultConfiguration(name)
        Class modelClass = configObject.model
        Map flatConfig = configObject.components.flatten()
        flatConfig.each {path, mode ->
            String fixedPath = (modelClass.simpleName - "Model") + ":" + path.replace(".", ":")
            configuration.collectors << new PacketCollector(path: getPathMapping(pathCache, fixedPath).pathName, mode: CollectingModeFactory.getStrategy(mode))
        }
        pathCache.clear()
        configuration.modelClass = modelClass

        return configuration.save() != null
    }

    private PathMapping getPathMapping(List<PathMapping> cache, String name) {
        PathMapping pathMapping = cache.find { it.pathName == name }
        if (!pathMapping) {
            pathMapping = PathMapping.findByPathName(name)
        }
        if (!pathMapping) {
            pathMapping = new PathMapping(pathName: name)
            saveDomainObject(pathMapping)
        }
        return pathMapping
    }

    private def saveDomainObject(def domainObject) {
        if (!domainObject.save()) {
            domainObject.errors.each {
                LOG.error it
            }
        }
    }

    public String prepare(URL file, String itemName) {
        GroovyUtils.parseGroovyScript readFromURL(file), { ConfigObject config ->
            configObject = config
        }
        if (configObject.containsKey("displayName")) {
            name = configObject.displayName
        } else {
            name = itemName - ".groovy"
        }
        return name
    }

    String getModelClassName() {
        return configObject.get("model").name
    }

}
