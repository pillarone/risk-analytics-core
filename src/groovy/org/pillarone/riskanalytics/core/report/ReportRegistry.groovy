package org.pillarone.riskanalytics.core.report

import com.google.common.collect.Multimap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.google.common.collect.ArrayListMultimap

abstract class ReportRegistry {

    private static Log LOG = LogFactory.getLog(ReportRegistry)

    private static Multimap<Class, IReportModel> reportMap = ArrayListMultimap.create()

    public static void registerReportModel(Class modelClass, IReportModel report) {
        Collection<IReportModel> existing = reportMap.get(modelClass)
        if (existing.find { it.class == report.class }) {
            LOG.warn("Report ${report.class.name} for $modelClass.name already registered.")
        }

        reportMap.put(modelClass, report)
    }

    public static List<IReportModel> getReportModel(Class modelClass) {
        return reportMap.get(modelClass)
    }

    public static List<IReportModel> getAllReportModels() {
        return reportMap.values().toList()
    }

}
