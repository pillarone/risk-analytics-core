package org.pillarone.riskanalytics.core.remoting.impl

import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.PostSimulationCalculation
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ParameterizationInfo
import org.pillarone.riskanalytics.core.remoting.ResultInfo
import org.pillarone.riskanalytics.core.remoting.SimulationInfo
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

class ResultService implements IResultService {

    List<ParameterizationInfo> getParameterizationInfosForTransactionId(long dealId) {
        List<ParameterizationInfo> result = []
        List<ParameterizationDAO> parameterizations = ParameterizationDAO.findAllByDealId(dealId)
        for (ParameterizationDAO dao in parameterizations) {
            result << new ParameterizationInfo(parameterizationId: dao.id, name: dao.name, version: dao.itemVersion, comment: dao.comment, user: dao.lastUpdater?.username)
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
        for (int i = 0; i < run.periodCount; i++) {
            for (String fullPath in getPathsOfResult(paths, run)) {
                ResultInfo info = new ResultInfo(path: fullPath, periodIndex: i)
                if (i < periodDates.size()) {
                    info.periodDate = periodDates[i]
                }
                List<ResultInfo.IterationValuePair> values = []
                int iteration = 0

                String path = fullPath.substring(0, fullPath.lastIndexOf(":"))
                String field = fullPath.substring(fullPath.lastIndexOf(":") + 1)
                for (Number value in ResultAccessor.getValues(run, i, path, AggregatedCollectingModeStrategy.IDENTIFIER, field)) {
                    values << new ResultInfo.IterationValuePair(iteration, value.toDouble())
                    iteration++
                }
                info.values = values
                result << info
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
        List<String> allPaths = PostSimulationCalculation.executeQuery("SELECT  path.pathName FROM ${PostSimulationCalculation.class.name} as p  WHERE p.run.id = ? ", [run.id])
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
            info.runDate = run.startTime
            info.user = "" //TODO
            info.iterationCount = run.iterations
            info.resultTemplateName = run.resultConfiguration.name
            info.comment = run.comment
            info.randomSeed = run.randomSeed

            result << info
        }
        return result
    }


}
