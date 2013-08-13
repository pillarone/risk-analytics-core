package org.pillarone.riskanalytics.core.modellingitem

import org.hibernate.event.*
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.simulation.item.Simulation

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
        ModellingItem item = ModellingItemMapper.getModellingItem(postInsertEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                listener.modellingItemAdded(item)
            }
        }
    }

    void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        ModellingItem item = ModellingItemMapper.getModellingItem(postUpdateEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                fireEvent(item, postUpdateEvent.entity, listener)
            }
        }
    }

    void fireEvent(ModellingItem item, entity, ModellingItemListener listener) {
        listener.modellingItemChanged(item)
    }

    void fireEvent(Simulation item, SimulationRun dao, ModellingItemListener listener) {
        if (dao.toBeDeleted) {
            listener.modellingItemDeleted(item)
        } else {
            listener.modellingItemChanged(item)

        }
    }

    void onPostDelete(PostDeleteEvent postDeleteEvent) {
        ModellingItem item = ModellingItemMapper.getModellingItem(postDeleteEvent.entity)
        if (item != null) {
            for (ModellingItemListener listener : _listeners) {
                listener.modellingItemDeleted(item)
            }
        }
    }
}
