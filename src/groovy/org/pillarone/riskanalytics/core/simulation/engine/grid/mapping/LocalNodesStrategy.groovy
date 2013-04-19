package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import groovy.transform.CompileStatic
import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode

@CompileStatic
class LocalNodesStrategy extends AbstractNodeMappingStrategy {

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {
        Set<GridNode> result = new HashSet<GridNode>()
        GridRichNode localNode = grid.localNode()
        Set<String> localAddresses = (localNode.externalAddresses() + localNode.internalAddresses()).toSet()

        for (GridNode node in allNodes) {
            Set<String> remoteAddresses = (node.externalAddresses() + node.internalAddresses()).toSet()
            if (remoteAddresses.any { localAddresses.contains(it) }) {
                result.add(node)
            }
        }

        return result
    }


}
