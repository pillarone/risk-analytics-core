package org.pillarone.riskanalytics.core.report

import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

abstract class ReportRegistry {

    private static Log LOG = LogFactory.getLog(ReportRegistry)

    private static Multimap<Class, IReportModel> reportMap = TreeMultimap.create()

    public static void registerAggregator(Class modelClass, IReportModel report) {
        Collection<IReportModel> existing = reportMap.get(modelClass)
        if (existing.find { it.class == report.class }) {
            LOG.warn("Report ${report.class.name} for $modelClass.name already registered.")
        }

        reportMap.put(modelClass, report)
    }

    public static List<IReportModel> getAggregator(Class modelClass) {
        return reportMap.get(modelClass)
    }

}
