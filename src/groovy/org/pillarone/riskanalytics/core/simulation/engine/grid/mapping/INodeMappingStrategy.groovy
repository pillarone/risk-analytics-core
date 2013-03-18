package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode


public interface INodeMappingStrategy {

    int getTotalCpuCount(List<GridNode> usableNodes)

    Set<GridNode> filterNodes(List<GridNode> allNodes)

}