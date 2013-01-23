package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

class ModelItem extends ModellingItem {
    VersionNumber versionNumber
    String srcCode

    public ModelItem(String name) {
        super(name)
        versionNumber = new VersionNumber('1')
    }

    protected Object createDao() {
        return new ModelDAO()
    }

    public Object getDaoClass() {
        ModelDAO
    }

    protected void mapToDao(def target) {
        target.name = name
        target.itemVersion = versionNumber.toString()
        target.srcCode = srcCode
        target.modelClassName = modelClass.name
    }

    protected void mapFromDao(def dao, boolean completeLoad) {
        if (dao) {
            versionNumber = new VersionNumber(dao.itemVersion)
            srcCode = dao.srcCode
            name = dao.name
            modelClass = ModelRegistry.instance.getModelClass(dao.modelClassName)
        }
    }

    protected def loadFromDB() {
        daoClass.findByNameAndItemVersion(name, versionNumber.toString())
    }

}