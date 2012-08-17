package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.components.Component


public interface IParametrizedItemListener {

    void componentAdded(String path, Component component)

    void componentRemoved(String path)

    void parameterValuesChanged(List<String> paths)

    void classifierChanged(String path)

}