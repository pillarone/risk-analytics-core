package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

class ModelItem extends ModellingItem {
    String srcCode

    public ModelItem(String name) {
        super(name)
        versionNumber = new VersionNumber('1')
    }

    @CompileStatic
    protected Object createDao() {
        return new ModelDAO()
    }

    @CompileStatic
    public Object getDaoClass() {
        ModelDAO
    }

    protected void mapToDao(def target) {
        ModelDAO modelDAO = target
        modelDAO.name = name
        modelDAO.itemVersion = versionNumber.toString()
        modelDAO.srcCode = srcCode
        modelDAO.modelClassName = modelClass.name
    }

    protected void mapFromDao(def dao, boolean completeLoad) {
        ModelDAO modelDAO = dao as ModelDAO
        if (modelDAO) {
            versionNumber = new VersionNumber(modelDAO.itemVersion)
            srcCode = modelDAO.srcCode
            name = modelDAO.name
            modelClass = ModelRegistry.instance.getModelClass(modelDAO.modelClassName)
        }
    }

    protected ModelDAO loadFromDB() {
        ModelDAO.findByNameAndItemVersion(name, versionNumber.toString())
    }
}