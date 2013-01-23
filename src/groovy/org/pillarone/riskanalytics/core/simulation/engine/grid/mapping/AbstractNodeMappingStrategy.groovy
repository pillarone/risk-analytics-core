package org.pillarone.riskanalytics.core.simulation.engine.grid.mapping

import org.gridgain.grid.GridNode
import org.gridgain.grid.Grid
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ConfigurationHolder


abstract class AbstractNodeMappingStrategy implements INodeMappingStrategy {

    public static final String STRATEGY_CLASS_SYSTEM_PROPERTY = "nodeMappingStrategy"
    public static final String STRATEGY_CLASS_KEY = STRATEGY_CLASS_SYSTEM_PROPERTY

    protected Grid grid

    protected Log LOG = LogFactory.getLog(getClass())

    AbstractNodeMappingStrategy() {
        this.grid = GridHelper.getGrid()
    }

    @Override
    int getTotalCpuCount(List<GridNode> usableNodes) {
        List<String> usedHosts = new ArrayList<String>();
        int processorCount = 0;
        for (GridNode node: usableNodes) {
            String ip = getAddress(node);
            if (!usedHosts.contains(ip)) {
                processorCount += node.metrics().getTotalCpus();
                usedHosts.add(ip);
            }
        }
        LOG.info("Found " + processorCount + " CPUs on " + usableNodes.size() + " nodes");
        return processorCount;
    }

    public static INodeMappingStrategy getStrategy() {
        try {
            Class strategy = ConfigurationHolder.config.get(STRATEGY_CLASS_KEY)
            if(System.getProperty(STRATEGY_CLASS_SYSTEM_PROPERTY) != null) {
                String mappingClass = System.getProperty(STRATEGY_CLASS_SYSTEM_PROPERTY)
                strategy = Thread.currentThread().contextClassLoader.loadClass(mappingClass)
            }
            return strategy.newInstance()
        } catch (Exception e) {
            return new LocalNodesStrategy()
        }

    }

    protected String getAddress(GridNode node) {
        if(!node.internalAddresses().empty) {
            return node.internalAddresses().toList().get(0);
        }
        if(!node.externalAddresses().empty) {
            return node.externalAddresses().toList().get(0);
        }
        throw new IllegalStateException("Grid node ${node} does not have a physical address.")
    }

}
