package org.pillarone.riskanalytics.core.modellingitem

import org.pillarone.riskanalytics.core.simulation.item.ModellingItem

/**
 * Created by IntelliJ IDEA.
 * User: bzetterstrom
 * Date: 11/4/11
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ModellingItemListener {
  void modellingItemAdded(ModellingItem item);
  void modellingItemDeleted(ModellingItem item);
  void modellingItemChanged(ModellingItem item);
}