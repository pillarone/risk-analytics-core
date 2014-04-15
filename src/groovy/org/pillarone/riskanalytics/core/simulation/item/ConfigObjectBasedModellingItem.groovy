package org.pillarone.riskanalytics.core.simulation.item
import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.pillarone.riskanalytics.core.util.GroovyUtils
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter

abstract class ConfigObjectBasedModellingItem extends ModellingItem {

    ConfigObject data
    String comment

    ConfigObjectBasedModellingItem(String name) {
        super(name);
        versionNumber = new VersionNumber('1')
    }


    abstract public IConfigObjectWriter getWriter()

    Class getModelClass() {
        Class modelClass = null
        if (data != null) {
            modelClass = data.containsKey("model") ? data.model : null
        }
        return modelClass
    }

    void setModelClass(Class newModelClass) {
        if (data == null) {
            data = new ConfigObject()
        }
        data.model = newModelClass
    }

    @CompileStatic
    private String checkAndTransformToStringData() {
        StringWriter stringWriter = new StringWriter()
        writer.write(data, new BufferedWriter(stringWriter))
        String stringData = stringWriter.toString()

        GroovyUtils.parseGroovyScript stringData, { }

        return stringData
    }

    @CompileStatic
    void unload() {
        super.unload();
        data = null
    }

    protected void mapToDao(def target) {
        target.name = name
        target.itemVersion = versionNumber.toString()
        target.modelClassName = modelClass?.name
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
