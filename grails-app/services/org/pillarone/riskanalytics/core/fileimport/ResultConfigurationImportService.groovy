package org.pillarone.riskanalytics.core.fileimport

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.CollectorInformation
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO

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
        ResultConfigurationDAO configuration = new ResultConfigurationDAO()
        configuration.name = name
        Class modelClass = configObject.model
        Map flatConfig = configObject.components.flatten()
        flatConfig.each {path, mode ->
            String fixedPath = (modelClass.simpleName - "Model") + ":" + path.replace(".", ":")
            PathMapping pathMapping = PathMapping.findByPathName(fixedPath)
            if (!pathMapping) {
                pathMapping = new PathMapping(pathName: fixedPath)
                saveDomainObject(pathMapping)
            }
            configuration.addToCollectorInformation(new CollectorInformation(path: pathMapping, collectingStrategyIdentifier: mode))
        }

        configuration.itemVersion = "1"
        configuration.modelClassName = modelClass.name
        saveDomainObject(configuration)

        return true
    }

    private def saveDomainObject(def domainObject) {
        if (!domainObject.save()) {
            domainObject.errors.each {
                LOG.error it
            }
        }
    }

    public String prepare(URL file, String itemName) {
        configObject = new ConfigSlurper().parse(readFromURL(file))
        if (configObject.containsKey("displayName")) {
            name = configObject.displayName
        } else {
            name = itemName - ".groovy"
        }
        return name
    }


}
