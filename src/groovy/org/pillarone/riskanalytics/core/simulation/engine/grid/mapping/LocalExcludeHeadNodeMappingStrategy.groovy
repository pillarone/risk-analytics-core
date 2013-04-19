package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import groovy.transform.CompileStatic
import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode

@CompileStatic
class LocalExcludeHeadNodeMappingStrategy extends LocalNodesStrategy {

    @Override
    int getTotalCpuCount(List<GridNode> usableNodes) {
        return usableNodes.size() //exactly one job per external node
    }

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {

        Set<GridNode> result = super.filterNodes(allNodes)

//        Remove the head node in the hopes that this preserves UI responsiveness.
        result.removeAll(allNodes.find { it.is(grid.localNode()) })
        return result
    }
}
