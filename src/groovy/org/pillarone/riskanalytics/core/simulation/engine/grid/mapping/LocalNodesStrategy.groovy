package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode


class LocalNodesStrategy extends AbstractNodeMappingStrategy {

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

        return result
    }


}
