package org.pillarone.riskanalytics.core.fileimport

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.util.ConfigObjectUtils
import org.pillarone.riskanalytics.core.ParameterizationDAO

abstract class FileImportService {

    protected static final Log LOG = LogFactory.getLog(FileImportService)

    abstract String getFileSuffix()

    abstract public def getDaoClass()

    abstract protected boolean saveItemObject(String fileContent)

    abstract String prepare(File file)

    public int compareFilesAndWriteToDB(List modelNames = null) {

        int recordCount = 0
        scanImportFolder(modelNames).each {File file ->
            if (importFile(file)) {
                recordCount++
            }
        }
        return recordCount
    }

    protected List scanImportFolder(List modelNames = null) {
        File modelSourceFolder = searchModelImportFolder()
        List matchingFiles = []
        modelSourceFolder.eachFileRecurse {File file ->
            if (file.isFile() && file.name.endsWith("${fileSuffix}.groovy") && shouldImportModel(file.name, modelNames)) {
                matchingFiles << file
            }
        }
        return matchingFiles
    }

    protected boolean importFile(File file) {
        LOG.debug("importing $file.name")
        boolean success = false
        String itemName = prepare(file)

        def fileContent = file.getText()
        boolean alreadyImported = lookUpItem(getDaoClass(), itemName)

        if (!alreadyImported) {
            if (saveItemObject(fileContent)) {
                LOG.debug(">imported $itemName")
                success = true
            } else {
                LOG.error("Error importing $itemName")
            }
        } else {
            LOG.debug("omitted $itemName as it already exists.")
        }
        return success
    }

    protected boolean lookUpItem(String itemName) {
        return getDaoClass().findByName(itemName) != null
    }

    protected boolean lookUpItem(def daoClass, String itemName) {
        if (daoClass == ParameterizationDAO) {
            return  getDaoClass().findByNameAndModelClassName(itemName, getModelClassName()) != null
        } else {
            return lookUpItem(itemName)
        }
    }

    protected boolean shouldImportModel(String filename, List models) {
        if (!models) {
            return true
        }
        models.any {String it ->
            filename.startsWith(it)
        }
    }

    protected File searchModelImportFolder() {
        URL modelFolder = getClass().getResource("/models")
        if (modelFolder == null) {
            throw new RuntimeException("Model folder not found")
        }
        File modelSourceFolder = new File(modelFolder.toURI())
        LOG.debug "modelSource: ${modelSourceFolder.path}"
        return modelSourceFolder
    }

    public static void spreadRanges(ConfigObject config) {
        ConfigObjectUtils.spreadRanges config
    }


    static void importModelsIfNeeded(List modelNames) {
        String models = modelNames != null && !modelNames.empty ? modelNames.join(", ") : "all models"
        LOG.info "Importing files for ${models}"
        new ParameterizationImportService().compareFilesAndWriteToDB(modelNames)
        new ModelStructureImportService().compareFilesAndWriteToDB(modelNames)
        new ModelFileImportService().compareFilesAndWriteToDB(modelNames)
        new ResultConfigurationImportService().compareFilesAndWriteToDB(modelNames)
    }

    String getModelClassName() {
        return null
    }

}