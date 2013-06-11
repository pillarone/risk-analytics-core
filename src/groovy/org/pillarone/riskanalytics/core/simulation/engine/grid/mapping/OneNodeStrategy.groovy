package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import groovy.transform.CompileStatic
import org.gridgain.grid.GridNode
import org.gridgain.grid.GridRichNode
@CompileStatic
class OneNodeStrategy extends AbstractNodeMappingStrategy {

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {
        Set<GridNode> result = new HashSet<GridNode>()
        GridRichNode localNode = grid.localNode()
        if (allNodes.contains(localNode)) {
            result.add(localNode)
        }
        return result
    }


}
