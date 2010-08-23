package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridFactory;

public class GridHelper {

    public static Grid getGrid() {
        if (ApplicationHolder.getApplication() == null)
            return GridFactory.getGrid();
        else
            return (Grid) ApplicationHolder.getApplication().getMainContext().getBean("grid");
    }
}
