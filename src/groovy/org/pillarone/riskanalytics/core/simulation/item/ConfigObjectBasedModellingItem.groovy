package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter

abstract class ConfigObjectBasedModellingItem extends ModellingItem {

    ConfigObject data
    VersionNumber versionNumber
    String comment

    public ConfigObjectBasedModellingItem(String name) {
        super(name);
        versionNumber = new VersionNumber('1')
    }


    abstract public IConfigObjectWriter getWriter() {
    }

    public Class getModelClass() {
        Class modelClass = null
        if (data != null) {
            modelClass = data.containsKey("model") ? data.model : null
        }
        return modelClass
    }

    public void setModelClass(Class newModelClass) {
        if (data == null) {
            data = new ConfigObject()
        }
        data.model = newModelClass
    }

    private String checkAndTransformToStringData() {
        StringWriter stringWriter = new StringWriter()
        getWriter().write(data, new BufferedWriter(stringWriter))
        String stringData = stringWriter.toString()

        new ConfigSlurper().parse(stringData)

        return stringData
    }

    public void unload() {
        super.unload();
        data = null
    }



    protected void mapToDao(def target) {
        target.name = name
        target.itemVersion = versionNumber.toString()
        target.modelClassName = getModelClass()?.name
        target.comment = comment

        String actualStringData = checkAndTransformToStringData()

        if (target.stringData == null) {
            target.stringData = new ConfigObjectHolder()
        }

        String persistentStringData = target.stringData?.data

        if (actualStringData != persistentStringData || name != target.name) {
            target.stringData.data = actualStringData
        }
    }

    protected void saveDependentData(Object daoToBeSaved) {
        saveDao(daoToBeSaved.stringData)
    }



    protected void deleteDependentData(Object daoToBeDeleted) {
        daoToBeDeleted.stringData.delete() // TODO (msh): error handling
    }

    protected def loadFromDB() {
        daoClass.findByNameAndItemVersion(name, versionNumber.toString())
    }

    protected void mapFromDao(def source, boolean completeLoad) {
        if (source) {
            data = new ConfigSlurper().parse(source.stringData.data)
            comment = source.comment
            FileImportService.spreadRanges(data)
        }
    }

}
