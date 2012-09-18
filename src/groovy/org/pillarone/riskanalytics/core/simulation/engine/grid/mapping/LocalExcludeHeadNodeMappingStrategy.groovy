package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode


class LocalExcludeHeadNodeMappingStrategy extends AbstractNodeMappingStrategy {

    @Override
    int getTotalCpuCount(List<GridNode> usableNodes) {
        return usableNodes.size() //exactly one job per external node
    }

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {

        Set<GridNode> result = new HashSet<GridNode>()
        GridRichNode localNode = grid.localNode()
        String localAddress = localNode.physicalAddress

        for (GridRichNode node in allNodes) {
            if (node.physicalAddress == localAddress) {
                result.add(node)
            }
        }

//        Remove the head node in the hopes that this preserves UI responsiveness.
        result.removeAll(allNodes.find { it.is(grid.localNode()) })
        return result
    }
}
