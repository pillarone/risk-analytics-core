package org.pillarone.riskanalytics.core.report.impl

import org.pillarone.riskanalytics.core.report.IReportData
import org.pillarone.riskanalytics.core.report.IReportDataVisitor
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem

/**
 * bzetterstrom
 */
class ModellingItemReportData implements IReportData {
    private ModellingItem item

    ModellingItemReportData(ModellingItem item) {
        this.item = item
    }

    void accept(IReportDataVisitor visitor) {
        visitor.visitModellingItemReportData(this)
    }

    @Override
    String toString() {
        return getClass().getName() + "[item: " + item + "]";
    }

    ModellingItem getItem() {
        return item
    }

    void setItem(ModellingItem item) {
        this.item = item
    }
}
