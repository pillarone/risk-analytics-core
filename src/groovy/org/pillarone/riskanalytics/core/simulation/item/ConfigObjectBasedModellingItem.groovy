package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.util.GroovyUtils

abstract class ConfigObjectBasedModellingItem extends ModellingItem {

    ConfigObject data
    VersionNumber versionNumber
    String comment

    @CompileStatic
    public ConfigObjectBasedModellingItem(String name) {
        super(name);
        versionNumber = new VersionNumber('1')
    }


    abstract public IConfigObjectWriter getWriter()

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

    @CompileStatic
    private String checkAndTransformToStringData() {
        StringWriter stringWriter = new StringWriter()
        getWriter().write(data, new BufferedWriter(stringWriter))
        String stringData = stringWriter.toString()

        GroovyUtils.parseGroovyScript stringData, { }

        return stringData
    }

    @CompileStatic
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
            GroovyUtils.parseGroovyScript source.stringData.data, { ConfigObject config ->
                data = config
            }
            comment = source.comment
            FileImportService.spreadRanges(data)
        }
    }

}
