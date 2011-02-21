package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode

class AllNodesStrategy extends AbstractNodeMappingStrategy {

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {
        return allNodes
    }


}
