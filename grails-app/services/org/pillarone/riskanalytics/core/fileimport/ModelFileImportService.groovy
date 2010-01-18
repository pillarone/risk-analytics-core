package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ModelDAO

/**
 * @author sebastian.cartier (at) intuitive-collaboration (dot) com
 */
class ModelFileImportService extends FileImportService {

    private String fileName

    final String fileSuffix = "Model"

    protected boolean saveItemObject(String srcCode) {
        ModelDAO dao = new ModelDAO()
        dao.name = fileName
        dao.srcCode = srcCode
        dao.itemVersion = "1"

        dao.save()
        return true
    }

    public getDaoClass() {
        ModelDAO
    }


    public String prepare(File file) {
        fileName = file.name - '.groovy'
        return fileName
    }
}