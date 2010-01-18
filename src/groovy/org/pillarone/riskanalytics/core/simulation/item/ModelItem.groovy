package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.ModelDAO

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
    }

    protected void mapFromDao(def dao) {
        if (dao) {
            versionNumber = new VersionNumber(dao.itemVersion)
            srcCode = dao.srcCode
            name = dao.name
        }
    }

    protected def loadFromDB() {
        daoClass.findByNameAndItemVersion(name, versionNumber.toString())
    }

}