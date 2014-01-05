package org.pillarone.riskanalytics.core.report

import com.google.common.collect.Multimap
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.google.common.collect.ArrayListMultimap

@CompileStatic
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

    /**
     * If we want to report on more than one model class, we only want reports valid for all model classes.
     * @param modelClasses models we are asking for registered reports from.
     * @return A list of reports which are registered against, and intersect with reports for the list of model classes.
     */
    public static List<IReportModel> getReportModel(List<Class> modelClasses) {

        List<IReportModel> iReportModels = new ArrayList<IReportModel>()
        List<IReportModel> allReports = getAllReportModels()
        for (Class clazz in modelClasses) {
            Collection<IReportModel> potentialReports = reportMap.get(clazz)

            // If you can explain why this intersection is not a NOOP pls contact me -fr
            // Possibly what was really wanted: to do was something like: iReportModels = iReportModels.intersect(potentialReports)
            // THAT would keep all the reports that are compatible every report model supplied.
            // But that might also return an empty set if things werent planned as carefully...
            iReportModels.addAll(potentialReports.intersect(allReports))
        }
        return iReportModels.flatten()
    }

    public static List<IReportModel> getAllReportModels() {
        return reportMap.values().toList()
    }

}
