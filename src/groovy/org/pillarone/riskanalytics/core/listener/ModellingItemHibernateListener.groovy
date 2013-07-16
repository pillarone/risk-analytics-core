package org.pillarone.riskanalytics.core.listener

import org.hibernate.event.PostDeleteEventListener
import org.hibernate.event.PostUpdateEventListener
import org.hibernate.event.PostInsertEventListener
import org.hibernate.event.PostUpdateEvent
import org.hibernate.event.PostInsertEvent
import org.hibernate.event.PostDeleteEvent
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.Resource
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO

class ModellingItemHibernateListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {
    final List<ModellingItemListener> _listeners = new ArrayList<ModellingItemListener>();

    void addModellingItemListener(ModellingItemListener listener) {
        assert !_listeners.contains(listener);
        _listeners.add(listener);
    }

    void removeModellingItemListener(ModellingItemListener listener) {
        _listeners.remove(listener);
    }

    void onPostInsert(PostInsertEvent postInsertEvent) {
        ModellingItem item = getModellingItem(postInsertEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                listener.modellingItemAdded(item)
            }
        }
    }

    void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        ModellingItem item = getModellingItem(postUpdateEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                fireEvent(item, postUpdateEvent.entity, listener)
            }
        }
    }

    void fireEvent(ModellingItem item, entity, ModellingItemListener listener) {
        listener.modellingItemChanged(item)
    }

    void fireEvent(Simulation item, SimulationRun dao, ModellingItemListener listener){
        if (dao.toBeDeleted){
            listener.modellingItemDeleted(item)
        }else {
            listener.modellingItemChanged(item)

        }
    }

    void onPostDelete(PostDeleteEvent postDeleteEvent) {
        ModellingItem item = getModellingItem(postDeleteEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                listener.modellingItemDeleted(item)
            }
        }
    }

    ModellingItem getModellingItem(Object dao) {
        null
    }

    Resource getModellingItem(ResourceDAO dao) {
        Resource resource = new Resource(dao.name, Thread.currentThread().contextClassLoader.loadClass(dao.resourceClassName))
        resource.versionNumber = new VersionNumber(dao.itemVersion)

        return resource
    }

    Parameterization getModellingItem(ParameterizationDAO dao) {
        Parameterization parameterization = new Parameterization(dao.name, Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName))
        parameterization.id = dao.id
        parameterization.versionNumber = new VersionNumber(dao.itemVersion)
        parameterization.creationDate = dao.creationDate
        parameterization.modificationDate = dao.modificationDate
        parameterization.creator = dao.creator
        parameterization.lastUpdater = dao.lastUpdater
        parameterization.tags = dao.tags*.tag
        parameterization.valid = dao.valid
        parameterization.status = dao.status
        return parameterization
    }

    ResultConfiguration getModellingItem(ResultConfigurationDAO dao) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(dao.name)
        resultConfiguration.id = dao.id
        resultConfiguration.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName)
        resultConfiguration.versionNumber = new VersionNumber(dao.itemVersion)
        resultConfiguration.creationDate = dao.creationDate
        resultConfiguration.modificationDate = dao.modificationDate
        resultConfiguration.creator = dao.creator
        resultConfiguration.lastUpdater = dao.lastUpdater
        return resultConfiguration
    }

    Simulation getModellingItem(SimulationRun dao) {
        Simulation simulation = new Simulation(dao.name)
        simulation.id = dao.id
        simulation.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.model)
        simulation.end = dao.endTime
        simulation.start = dao.startTime
        simulation.creationDate = dao.creationDate
        simulation.modificationDate = dao.modificationDate
        simulation.creator = dao.creator
        return simulation
    }
}
