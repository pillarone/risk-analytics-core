package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.gridgain.grid.Grid;

public class GridHelper {

    public static Grid getGrid() {
        return (Grid) ApplicationHolder.getApplication().getMainContext().getBean("grid");
    }
}
