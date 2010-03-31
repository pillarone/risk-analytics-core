package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ModelStructureDAO

import org.pillarone.riskanalytics.core.output.ConfigObjectHolder

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ModelStructureImportService extends FileImportService {

    private ConfigObject currentConfigObject

    final String fileSuffix = "Structure"

    protected boolean saveItemObject(String fileContent) {

        ModelStructureDAO dao = new ModelStructureDAO()
        dao.name = currentConfigObject.displayName
        dao.modelClassName = currentConfigObject.model.name
        dao.stringData = new ConfigObjectHolder()
        dao.stringData.data = fileContent
        dao.stringData.save()
        dao.itemVersion = "1"

        dao.save()
        return true
    }

    public getDaoClass() {
        ModelStructureDAO
    }

    public String prepare(URL file, String itemName) {
        currentConfigObject = new ConfigSlurper().parse(readFromURL(file))
        String name = itemName - ".groovy"
        if (currentConfigObject.containsKey('displayName')) {
            name = currentConfigObject.displayName
        } else {
            currentConfigObject.displayName = name
        }
        return name
    }


}