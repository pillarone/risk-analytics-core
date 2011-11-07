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
        assert ! _listeners.contains(listener);
        _listeners.add(listener);
    }

    void removeModellingItemListener(ModellingItemListener listener) {
        _listeners.remove(listener);
    }

    void onPostInsert(PostInsertEvent postInsertEvent) {
        ModellingItem item = getModellingItem(postInsertEvent.entity)
        if (item != null) {
            for(ModellingItemListener listener : _listeners) {
                listener.modellingItemAdded(item)
            }
        }
    }

    void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        ModellingItem item = getModellingItem(postUpdateEvent.entity)
        if (item != null) {
            for(ModellingItemListener listener : _listeners) {
                listener.modellingItemChanged(item)
            }
        }
    }

    void onPostDelete(PostDeleteEvent postDeleteEvent) {
        ModellingItem item = getModellingItem(postDeleteEvent.entity)
        if (item != null) {
            for(ModellingItemListener listener : _listeners) {
                listener.modellingItemDeleted(item)
            }
        }
    }

    ModellingItem getModellingItem(Object entity) {
        // todo: add other relevant ModellingItem subclasses here, such as ResultConfiguration and Simulation
        if (entity instanceof ParameterizationDAO) {
            return getItem((ParameterizationDAO)entity)
        } else {
            return null;
        }
    }

    // todo: this code is based on a call in the class
    //    org.pillarone.riskanalytics.application.dataaccess.item.ModellingItemFactory
    // refactoring needed (we can not have dependencies on the RiskAnalyticsApplication package here
    private ModellingItem getItem(ParameterizationDAO dao, Class modelClass = null) {
        Parameterization item = new Parameterization(dao.name)
        item.versionNumber = new VersionNumber(dao.itemVersion)
        // PMO-645 set valid  for parameterization check
        item.valid = dao.valid
        item.status = dao.status
        if (modelClass != null) {
            item.modelClass = modelClass
            item.creator = dao.creator
            if (item.creator)
                item.creator.username = dao.creator.username
            item.lastUpdater = dao.lastUpdater
            if (item.lastUpdater)
                item.lastUpdater.username = dao.lastUpdater.username
            item.creationDate = dao.getCreationDate()
            item.modificationDate = dao.getModificationDate()
            item.tags = dao.tags*.tag
        }
        item
    }

}
