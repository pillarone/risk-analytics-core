package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ParameterizationDAO

import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

public class ParameterizationImportService extends FileImportService {

    protected ConfigObject currentConfigObject

    final String fileSuffix = "Parameters"

    protected boolean saveItemObject(String fileContent) {
        Parameterization result = ParameterizationHelper.createParameterizationFromConfigObject(currentConfigObject, currentConfigObject.displayName)
        if (!result.save()) {
            return false
        }

        return true
    }


    public getDaoClass() {
        ParameterizationDAO
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

    String getModelClassName() {
        return currentConfigObject.get("model").name
    }


}