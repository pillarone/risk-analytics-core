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
                listener.modellingItemChanged(item)
            }
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
        parameterization.versionNumber = new VersionNumber(dao.itemVersion)
        return parameterization
    }

    ResultConfiguration getModellingItem(ResultConfigurationDAO dao) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(dao.name)
        resultConfiguration.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName)
        resultConfiguration.versionNumber = new VersionNumber(dao.itemVersion)
        return resultConfiguration
    }

    Simulation getModellingItem(SimulationRun dao) {
        Simulation simulation = new Simulation(dao.name)
        simulation.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.model)
        return simulation
    }
}
