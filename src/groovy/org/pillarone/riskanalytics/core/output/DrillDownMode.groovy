package org.pillarone.riskanalytics.core.output

public enum DrillDownMode {
    BY_SOURCE,
    BY_PERIOD,
    BY_TYPE

    static List<DrillDownMode> getDrillDownModesBySource() {
        List<DrillDownMode> drillDownModes = new ArrayList<DrillDownMode>()
        drillDownModes.add(DrillDownMode.BY_SOURCE)
        return drillDownModes
    }

    static List<DrillDownMode> getDrillDownModesByPeriod() {
        List<DrillDownMode> drillDownModes = new ArrayList<DrillDownMode>()
        drillDownModes.add(DrillDownMode.BY_PERIOD)
        return drillDownModes
    }

    static List<DrillDownMode> getDrillDownModesByType() {
        List<DrillDownMode> drillDownModes = new ArrayList<DrillDownMode>()
        drillDownModes.add(DrillDownMode.BY_TYPE)
        return drillDownModes
    }
}