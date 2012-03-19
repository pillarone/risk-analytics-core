package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridFactory;
import org.pillarone.riskanalytics.core.FileConstants;

import java.io.File;

public class GridHelper {

    public static Grid getGrid() {
        try {
            return (Grid) ApplicationHolder.getApplication().getMainContext().getBean("grid");
        } catch (Exception e) {
            return GridFactory.getGrid();
        }
    }

    public static String getResultLocation(long runId) {
        return FileConstants.EXTERNAL_DATABASE_DIRECTORY + File.separator + "simulations" + File.separator + runId;
    }

    public static String getResultPathLocation(long runId, long pathId, long fieldId, long collectorId, int period) {
        return getResultLocation(runId) + File.separator + pathId + "_" + period + "_" + fieldId + "_" + collectorId;
    }
}
