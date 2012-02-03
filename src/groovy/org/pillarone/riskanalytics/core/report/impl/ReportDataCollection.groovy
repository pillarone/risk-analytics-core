package org.pillarone.riskanalytics.core.report.impl

import org.pillarone.riskanalytics.core.report.IReportData
import org.pillarone.riskanalytics.core.report.IReportDataVisitor

/**
 * bzetterstrom
 */
class ReportDataCollection implements IReportData, Iterable<IReportData> {
    List<IReportData> collection


    ReportDataCollection(Collection<IReportData> collection) {
        this.collection = new ArrayList<IReportData>(collection)
    }

    void accept(IReportDataVisitor visitor) {
        visitor.visitReportDataCollection(this)
    }

    int size() {
        collection.size()
    }

    IReportData get(int index) {
        collection.get(index)
    }

    Iterator<IReportData> iterator() {
        return collection.iterator()
    }
}
