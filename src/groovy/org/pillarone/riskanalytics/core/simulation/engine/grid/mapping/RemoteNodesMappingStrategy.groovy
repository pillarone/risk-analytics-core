package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import groovy.transform.CompileStatic
import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode

/**
 * For use when no local gridnodes should be given work, only remote nodes.
 * Not much use unless we can minimise the classloading over the network.
 *
 * User: frahman
 * Date: 21.10.13
 * Time: 16:45
 */
@CompileStatic
class RemoteNodesMappingStrategy extends AbstractNodeMappingStrategy {


    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {

        Set<GridNode> remoteNodes = new HashSet<GridNode>();

        GridRichNode localNode = grid.localNode();
        Set<String> localAddresses = (localNode.externalAddresses() + localNode.internalAddresses()).toSet();

        for (GridNode node in allNodes) {
            Set<String> nodeAddresses = (node.externalAddresses() + node.internalAddresses()).toSet()
            if ( ! nodeAddresses.any { localAddresses.contains(it) }) {
                remoteNodes.add(node);
            }
        }

        return remoteNodes;

    }
}
