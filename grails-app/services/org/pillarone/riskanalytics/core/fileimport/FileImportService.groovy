package org.pillarone.riskanalytics.core.fileimport

import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.util.ConfigObjectUtils
import org.joda.time.DateTimeZone
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

abstract class FileImportService {

    protected static final Log LOG = LogFactory.getLog(FileImportService)

    abstract String getFileSuffix()

    abstract public def getDaoClass()

    abstract protected boolean saveItemObject(String fileContent)

    abstract String prepare(URL file, String itemName)

    /** Setting the default time zone to UTC avoids problems in multi user context with different time zones
     *  and switches off daylight saving capabilities and possible related problems. */
    DateTimeZone utc = DateTimeZone.setDefault(DateTimeZone.UTC)

    public int compareFilesAndWriteToDB(List modelNames = null) {

        int recordCount = 0
        scanImportFolder(modelNames).each {URL url ->
            if (importFile(url)) {
                recordCount++
            }
        }
        return recordCount
    }

    protected List scanImportFolder(List modelNames = null) {
        URL modelSourceFolder = searchModelImportFolder()
        return modelSourceFolder.toExternalForm().startsWith("jar") ? findURLsInJar(modelSourceFolder, modelNames) : findURLsInDirectory(modelSourceFolder, modelNames)
    }

    protected List<URL> findURLsInDirectory(URL url, List modelNames) {
        LOG.trace "Importing from directory ${url.toExternalForm()}"

        List<URL> matchingFiles = []
        new File(url.toURI()).eachFileRecurse {File file ->
            if (file.isFile() && file.name.endsWith("${fileSuffix}.groovy") && shouldImportModel(file.name, modelNames)) {
                matchingFiles << file.toURI().toURL()
            }
        }
        return matchingFiles
    }

    protected List<URL> findURLsInJar(URL url, List modelNames) {
        LOG.trace "Importing from JAR file ${url.toExternalForm()}"

        List<URL> matchingFiles = []

        JarURLConnection connection
        ZipInputStream inputStream

        try {
            connection = (JarURLConnection) url.openConnection()
            URL jarUrl = connection.getJarFileURL()
            inputStream = new JarInputStream(jarUrl.openStream())
            ZipEntry entry = null
            while ((entry = inputStream.getNextEntry()) != null) {
                String entryName = entry.getName()
                if (entryName.contains("models") && entryName.endsWith("${fileSuffix}.groovy") && shouldImportModel(entryName.substring(entryName.lastIndexOf("/") + 1), modelNames)) {
                    URL resource = getClass().getResource("/" + entryName)
                    if (resource != null) {
                        matchingFiles << resource
                    }
                }
                inputStream.closeEntry()

            }
        } finally {
            inputStream.close()
        }

        return matchingFiles
    }

    protected boolean importFile(URL url) {
        LOG.debug("importing ${url.toExternalForm()}")
        boolean success = false
        String urlString = url.toExternalForm()
        String itemName = prepare(url, urlString.substring(urlString.lastIndexOf("/") + 1))

        def fileContent = readFromURL(url)
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

    protected String readFromURL(URL url) {
        Scanner scanner = new Scanner(url.openStream()).useDelimiter("\\Z")
        return scanner.next()
    }

    protected boolean lookUpItem(String itemName) {
        return getDaoClass().findByName(itemName) != null
    }

    protected boolean lookUpItem(def daoClass, String itemName) {
        if (daoClass == ParameterizationDAO || daoClass == ResultConfigurationDAO) {
            return getDaoClass().findByNameAndModelClassName(itemName, getModelClassName()) != null
        } else {
            return lookUpItem(itemName)
        }
    }

    protected boolean shouldImportModel(String filename, List models) {
        if (!models) {
            return true
        }
        LOG.trace "filtering $filename with $models"
        models.any {String it ->
            filename.startsWith(it)
        }
    }

    protected URL searchModelImportFolder() {
        URL modelFolder = getClass().getResource("/models")
        if (modelFolder == null) {
            throw new RuntimeException("Model folder not found")
        }
        LOG.debug "Model source URL: ${modelFolder.toExternalForm()}"
        return modelFolder
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
        ModelRegistry.instance.loadFromDatabase()
    }

    String getModelClassName() {
        return null
    }

}