package org.pillarone.riskanalytics.core.listener

import org.hibernate.event.PostDeleteEventListener
import org.hibernate.event.PostUpdateEventListener
import org.hibernate.event.PostInsertEventListener
import org.hibernate.event.PostUpdateEvent
import org.hibernate.event.PostInsertEvent
import org.hibernate.event.PostDeleteEvent
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO

/**
 * Created by IntelliJ IDEA.
 * User: bzetterstrom
 * Date: 11/3/11
 * Time: 6:23 PM
 * To change this template use File | Settings | File Templates.
 */
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

    ModellingItem getModellingItem(Object entity) {
        if (entity instanceof ParameterizationDAO) {
            return toParameterization(entity)
        } else if (entity instanceof ResultConfigurationDAO) {
            return toResultConfiguration(entity)
        } else if (entity instanceof SimulationRun) {
            return toSimulation(entity)
        }

        return null
    }

    private Parameterization toParameterization(ParameterizationDAO dao) {
        Parameterization parameterization = new Parameterization(dao.name, Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName))
        parameterization.versionNumber = new VersionNumber(dao.itemVersion)

        return parameterization
    }

    private ResultConfiguration toResultConfiguration(ResultConfigurationDAO dao) {
        ResultConfiguration resultConfiguration = new ResultConfiguration(dao.name)
        resultConfiguration.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.modelClassName)
        resultConfiguration.versionNumber = new VersionNumber(dao.itemVersion)

        return resultConfiguration
    }

    private Simulation toSimulation(SimulationRun dao) {
        Simulation simulation = new Simulation(dao.name)
        simulation.modelClass = Thread.currentThread().contextClassLoader.loadClass(dao.model)

        return simulation
    }

}
