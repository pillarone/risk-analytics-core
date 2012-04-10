package org.pillarone.riskanalytics.core.remoting.impl

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

class ResultService implements IResultService {

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
            //period label aren't dates
        }
        for (int periodIndex = 0; periodIndex < run.periodCount; periodIndex++) {
            for (String fullPath in getPathsOfResult(paths, run, periodIndex)) {
                ResultInfo info = new ResultInfo(path: fullPath, periodIndex: periodIndex)
                if (periodIndex < periodDates.size()) {
                    info.periodDate = periodDates[periodIndex]
                }
                List<ResultInfo.IterationValuePair> values = []

                String path = fullPath.substring(0, fullPath.lastIndexOf(":"))
                String field = fullPath.substring(fullPath.lastIndexOf(":") + 1)

                List<SingleValueResult> results = ResultAccessor.getSingleValueResultsWithDateSkipZeroes(run, periodIndex, path, SingleValueCollectingModeStrategy.IDENTIFIER, field)

                if (! results.isEmpty()) {
                    for (SingleValueResult singleValueResult in results) {
                        Double value = singleValueResult.getValue().toDouble()
                        values << new ResultInfo.IterationValuePair(
                                singleValueResult.getIteration(),
                                value,
                                periodDates[periodIndex],
                        singleValueResult.getDate())
                    }
                    info.values = values
                    result << info
                }
            }
        }
        return result
    }

    /**
     * @param paths
     * @return paths where with regEx is replaced with existing subcomponents in a specific result
     */
    private List<String> getPathsOfResult(List<String> paths, SimulationRun run, int periodIndex) {
        if (!regExIsUsed(paths)) return paths
        Set<String> fullPaths = new HashSet<String>()
        List<String> allPaths = PostSimulationCalculation.executeQuery("SELECT  path.pathName FROM ${PostSimulationCalculation.class.name} as p  WHERE p.run.id = ? ", [run.id])
        if (allPaths.isEmpty()) {
            // try to get the pathnames from the symbolic single value results table instead -- a bit slower probably
            allPaths.addAll(getAllPaths(run, periodIndex))
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

    private List<String> getAllPaths(SimulationRun simulationRun, int periodIndex) {
        List<String> paths = []

        def results = SymbolicValueResult.executeQuery("SELECT distinct(path) FROM ${SymbolicValueResult.name} " +
                "WHERE simulation_run_id = ? and value <> 0.0 and period = ${periodIndex}", [simulationRun.id])
        for (def s: results) {
            paths << s
        }
        return paths
    }

    boolean regExIsUsed(List<String> paths) {
        for (String searchedPath: paths) {
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
