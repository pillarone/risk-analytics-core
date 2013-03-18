package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode


class LocalExcludedMappingStrategy extends AbstractNodeMappingStrategy {

    @Override
    int getTotalCpuCount(List<GridNode> usableNodes) {
        return usableNodes.size() //exactly one job per external node
    }

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {
        return allNodes.findAll { !it.is(grid.localNode()) }
    }
}
