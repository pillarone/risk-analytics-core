package org.pillarone.riskanalytics.core.remoting.impl

import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ResultInfo
import org.pillarone.riskanalytics.core.remoting.SimulationInfo
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

class ResultService implements IResultService {

    List<Long> getParameterizationIdsForTransactionId(long dealId) {
        return ParameterizationDAO.findAllByDealId(dealId)*.id
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
            for (String fullPath in paths) {
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
