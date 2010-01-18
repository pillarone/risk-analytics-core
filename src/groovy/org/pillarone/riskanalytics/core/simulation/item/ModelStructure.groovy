package org.pillarone.riskanalytics.core.simulation.item

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.ModelStructureWriter

import org.pillarone.riskanalytics.core.util.IConfigObjectWriter

public class ModelStructure extends ConfigObjectBasedModellingItem {
    protected static final Log LOG = LogFactory.getLog(ModelStructure)

    public ModelStructure(String name) {
        super(name)
    }

    protected Object createDao() {
        return new ModelStructureDAO();
    }

    public Object getDaoClass() {
        ModelStructureDAO
    }

    public IConfigObjectWriter getWriter() {
        return new ModelStructureWriter();
    }

    static ModelStructure getStructureForModel(Class modelClass) {
        ModelStructureDAO dbObject = ModelStructureDAO.findByModelClassName(modelClass.name)
        ModelStructure result = new ModelStructure(dbObject.name)
        result.versionNumber = new VersionNumber(dbObject.itemVersion)
        result.load()
        return result
    }

    static List findAllModelClasses() {
        def c = ModelStructureDAO.createCriteria()
        List modelClassNames = c.list {
            projections {
                property("modelClassName")
            }
        }

        List availableModelClasses = []
        def modelFilter = ApplicationHolder.application.getConfig()?.models

        modelClassNames.each {
            try {
                Class modelClass = Model.class.classLoader.loadClass(it)
                if (!modelFilter || modelFilter.contains(modelClass.simpleName)) {
                    availableModelClasses << modelClass
                }
            } catch (java.lang.ClassNotFoundException e) {
                LOG.error "Model ${it} is in db, but dont exist as classfile"
            }
        }

        LOG.debug "Found model classes:$modelClassNames"
        LOG.debug "Model filter specified: $modelFilter"
        LOG.debug "Available model classes: $availableModelClasses"

        return availableModelClasses
    }
}
