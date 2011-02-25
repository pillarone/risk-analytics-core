package org.pillarone.riskanalytics.core.model.registry

import org.pillarone.riskanalytics.core.ModelStructureDAO
import org.pillarone.riskanalytics.core.ModelDAO
import java.util.concurrent.CopyOnWriteArrayList
import org.pillarone.riskanalytics.core.output.ConfigObjectHolder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.util.concurrent.CopyOnWriteArraySet
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure


class ModelRegistry {

    private static ModelRegistry registry

    public static ModelRegistry getInstance() {
        if (registry == null) {
            registry = new ModelRegistry()
        }
        return registry
    }

    private Log LOG = LogFactory.getLog(ModelRegistry)
    private Set<Class> modelClasses

    private Set<IModelRegistryListener> listeners

    private ModelRegistry() {
        listeners = new HashSet<IModelRegistryListener>()
        modelClasses = new CopyOnWriteArraySet<Class>()
    }

    void addModel(Class modelClass) {

        if (modelClasses.contains(modelClass)) {
            LOG.warn " Model $modelClass.simpleName already registered"
            return
        }

        try {
            LOG.info "Registering model $modelClass.name"
            ModelStructureDAO structure = ModelStructureDAO.findByModelClassName(modelClass.name)
            ModelDAO model = ModelDAO.findByName(modelClass.simpleName)
            if (structure == null) {
                LOG.info "No structure found for model $modelClass - Creating default"
                createDefaultStructure(modelClass)
            }
            if (model == null) {
                LOG.info "No model dao found for model $modelClass - Creating default"
                createDefaultModelDao(modelClass)
            }
            modelClasses << modelClass
            notifyListeners(modelClass)
        } catch (Exception e) {
            throw new ModelRegistryException("Failed to register model", e)
        }
    }

    void loadFromDatabase() {
        for (Class clazz in ModelStructure.findAllModelClasses()) {
            addModel(clazz)
        }
    }

    private void createDefaultModelDao(Class modelClass) {
        ModelDAO dao = new ModelDAO()
        dao.name = modelClass.simpleName
        dao.srcCode = "//source code not available"
        dao.itemVersion = "1"

        if (!dao.save()) {
            throw new ModelRegistryException("Could not create default model dao for $modelClass.simpleName" + dao.errors.toString())
        }
    }

    private void createDefaultStructure(Class modelClass) {
        StringBuilder builder = new StringBuilder()
        builder.append("package ").append(modelClass.getPackage().name).append("\n").append("model = ").append(modelClasses.name).append("\n").append("company { }")

        ModelStructureDAO dao = new ModelStructureDAO()
        dao.name = modelClass.simpleName + "Structure"
        dao.modelClassName = modelClass.name
        dao.stringData = new ConfigObjectHolder()
        dao.stringData.data = builder.toString()
        dao.stringData.save()
        dao.itemVersion = "1"

        if (!dao.save()) {
            throw new ModelRegistryException("Could not create default structure for $modelClass.simpleName" + dao.errors.toString())
        }
    }

    void addListener(IModelRegistryListener listener) {
        listeners << listener
    }

    void removeListener(IModelRegistryListener listener) {
        listeners.remove(listener)
    }

    Set<Class> getAllModelClasses() {
        return Collections.unmodifiableSet(modelClasses)
    }

    private void notifyListeners(Class newModelClass) {
        for (IModelRegistryListener listener: listeners) {
            listener.modelAdded(newModelClass)
        }
    }
}
