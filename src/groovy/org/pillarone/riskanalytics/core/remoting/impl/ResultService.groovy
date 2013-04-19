package org.pillarone.riskanalytics.core.remoting.impl

import groovy.transform.CompileStatic

import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.PostSimulationCalculation
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ParameterizationInfo
import org.pillarone.riskanalytics.core.remoting.ResultInfo
import org.pillarone.riskanalytics.core.remoting.SimulationInfo
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.joda.time.DateTime

import org.pillarone.riskanalytics.core.simulation.item.Simulation

import org.pillarone.riskanalytics.core.output.SymbolicValueResult
import org.pillarone.riskanalytics.core.remoting.TagInfo
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO
import org.pillarone.riskanalytics.core.dataaccess.ExportResultAccessor
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class ResultService implements IResultService {

    private static Log LOG = LogFactory.getLog(ResultService.class)

    List<ParameterizationInfo> getParameterizationInfosForTransactionId(long dealId) {
        List<ParameterizationInfo> result = []
        List<ParameterizationDAO> parameterizations = ParameterizationDAO.findAllByDealId(dealId)
        for (ParameterizationDAO dao in parameterizations) {
            List<TagInfo> tags = dao.tags*.getTag().name as List<TagInfo>;
            result << new ParameterizationInfo(
                    parameterizationId: dao.id, name: dao.name, version: dao.itemVersion,
                    comment: dao.comment, user: dao.lastUpdater?.username, status: dao.status,
                    tags: tags

            )
        }
        return result
    }

    List<ResultInfo> getResults(long simulationId, List<String> paths) {
        List<ResultInfo> result = []
        List<Date> periodDates = []

        SimulationRun run = SimulationRun.get(simulationId)
        try {
            String periodLabels = run.parameterization.periodLabels
            String[] labels = periodLabels.split(";")
            SimpleDateFormat dateFormat = new SimpleDateFormat(Parameterization.PERIOD_DATE_FORMAT)
            for (String label in labels) {
                periodDates << dateFormat.parse(label)
            }
        } catch (Exception e) {
            LOG.error(e)
        }

        for (String fullPath in getPathsOfResult(paths, run)) {

            String path = fullPath.substring(0, fullPath.lastIndexOf(":"))
            String field = fullPath.substring(fullPath.lastIndexOf(":") + 1)
            List<SingleValueResultPOJO> results = ExportResultAccessor.getSingleValueResultsForExport(
                    SingleValueCollectingModeStrategy.IDENTIFIER, path, field, run)

            for (int periodIndex = 0; periodIndex < run.periodCount; periodIndex++) {
                ResultInfo info = new ResultInfo(path: fullPath, periodIndex: periodIndex)
                if (periodIndex < periodDates.size()) {
                    info.periodDate = periodDates[periodIndex]
                }
                List<ResultInfo.IterationValuePair> values = []
                List<SingleValueResultPOJO> resultsInPeriod = results.findAll { it -> it.getPeriod() == periodIndex }

                if (!results.isEmpty()) {
                    for (SingleValueResultPOJO singleValueResult in resultsInPeriod) {
                        if (singleValueResult.getValue() != 0) {
                            values << new ResultInfo.IterationValuePair(
                                    singleValueResult.getIteration(),
                                    singleValueResult.getValue(),
                                    periodDates[periodIndex],
                                    singleValueResult.getDate())
                        }
                    }
                    if (values.size() != 0) {
                        info.values = values
                        result << info
                    }
                }
            }
        }
        return result
    }

    /**
     * @param paths
     * @return paths where with regEx is replaced with existing subcomponents in a specific result
     */
    private List<String> getPathsOfResult(List<String> paths, SimulationRun run) {
        if (!regExIsUsed(paths)) return paths
        Set<String> fullPaths = new HashSet<String>()
        List<String> allPaths = PostSimulationCalculation.executeQuery("SELECT distinct path.pathName FROM ${PostSimulationCalculation.class.name} as p  WHERE p.run.id = ? ", [run.id])
        if (allPaths.isEmpty()) {
            // try to get the pathnames from the symbolic single value results table instead -- a bit slower probably
            allPaths.addAll(getAllPaths(run))
        }
        for (int i = 0; i < paths.size(); i++) {
            String field = paths[i].substring(paths[i].lastIndexOf(":") + 1)
            String searchedPath = paths[i].substring(0, paths[i].lastIndexOf(":"))
            if (searchedPath.indexOf("(.*)") != -1) {
                for (int j = 0; j < allPaths.size(); j++) {
                    String path = allPaths[j]
                    def matcher = path =~ searchedPath
                    if (matcher != null && matcher.size() > 0) {
                        fullPaths << matcher[0][0] + ":" + field
                    }
                }
            } else
                fullPaths << paths[i]
        }
        return fullPaths as List<String>;
    }

    private List<String> getAllPaths(SimulationRun simulationRun) {
        List<String> paths = []

        def results = SymbolicValueResult.executeQuery("SELECT distinct(path) FROM ${SymbolicValueResult.name} " +
                "WHERE simulation_run_id = ? and value <> 0.0 ", [simulationRun.id])
        for (def s : results) {
            paths << s
        }
        return paths
    }

    @CompileStatic
    boolean regExIsUsed(List<String> paths) {
        for (String searchedPath : paths) {
            if (searchedPath.indexOf("(.*)") != -1) {
                return true
            }
        }
        return false
    }

    List<SimulationInfo> getSimulationInfos(long parameterizationId) {
        List<SimulationRun> runs = SimulationRun.findAllByParameterization(ParameterizationDAO.get(parameterizationId))

        List<SimulationInfo> result = []
        for (SimulationRun run in runs) {
            SimulationInfo info = new SimulationInfo()
            info.simulationId = run.id
            info.name = run.name
            info.runDate = run.startTime?.toDate()
            info.user = "" //TODO
            info.iterationCount = run.iterations
            info.resultTemplateName = run.resultConfiguration.name
            info.comment = run.comment
            info.randomSeed = run.randomSeed

            def simulation = new Simulation(run.name)
            simulation.load()
            List<TagInfo> tags = simulation.getTags()*.name as List<TagInfo>;
            info.setTags(tags)

            // todo: this is from the art-models plugin really, so doesn't belong here at all... But keep for now,
            // since there are more important things to work on
            info.updateDate = ((DateTime) simulation.getParameter("runtimeUpdateDate")).toDate()

            result << info
        }
        return result
    }
}
