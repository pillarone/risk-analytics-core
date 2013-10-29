package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import groovy.transform.CompileStatic
import org.gridgain.grid.GridNode

@CompileStatic
class AllNodesStrategy extends AbstractNodeMappingStrategy {

    @Override
    Set<GridNode> filterNodes(List<GridNode> allNodes) {
        return allNodes.toSet()
    }


}
