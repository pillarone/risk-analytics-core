package org.pillarone.riskanalytics.core.remoting.impl

import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import models.core.CoreModel
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.remoting.SimulationInfo
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResult
import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.remoting.ResultInfo
import org.pillarone.riskanalytics.core.example.model.EmptyModel


class ResultServiceTests extends GroovyTestCase {

    IResultService resultService

    PathMapping path1
    PathMapping path2
    FieldMapping field
    CollectorMapping collector1

    void setUp() {
        path1 = PathMapping.findByPathName("testPath1")
        if (path1 == null) {
            path1 = new PathMapping(pathName: 'testPath1').save()
        }

        path2 = PathMapping.findByPathName('testPath2')
        if (path2 == null) {
            path2 = new PathMapping(pathName: 'testPath2').save()
        }

        field = FieldMapping.findByFieldName('value')
        if (field == null) {
            field = new FieldMapping(fieldName: 'value').save()
        }

        collector1 = CollectorMapping.findByCollectorName(AggregatedCollectingModeStrategy.IDENTIFIER)
        if (collector1 == null) {
            collector1 = new CollectorMapping(collectorName: AggregatedCollectingModeStrategy.IDENTIFIER).save()
        }

    }


    void testGetParamIds() {
        Parameterization parameterization1 = new Parameterization("test1")
        parameterization1.modelClass = CoreModel
        parameterization1.dealId = 1
        parameterization1.periodCount = 1
        parameterization1.save()

        Parameterization parameterization2 = new Parameterization("test2")
        parameterization2.modelClass = CoreModel
        parameterization2.dealId = 1
        parameterization2.periodCount = 1
        parameterization2.save()

        Parameterization parameterization3 = new Parameterization("test2")
        parameterization3.modelClass = CoreModel
        parameterization3.dealId = 2
        parameterization3.periodCount = 1
        parameterization3.save()

        List ids = resultService.getParameterizationIdsForTransactionId(1)

        assertEquals 2, ids.size()
        assertTrue ids.contains(parameterization1.id)
        assertTrue ids.contains(parameterization2.id)
    }

    void testGetSimulationInfos() {
        Parameterization parameterization1 = new Parameterization("test1")
        parameterization1.modelClass = CoreModel
        parameterization1.dealId = 1
        parameterization1.periodCount = 1
        parameterization1.save()

        ResultConfiguration resultConfiguration = new ResultConfiguration("rc1")
        resultConfiguration.modelClass = CoreModel
        resultConfiguration.save()

        Simulation simulation = new Simulation("s1")
        simulation.modelClass = CoreModel
        simulation.parameterization = parameterization1
        simulation.template = resultConfiguration
        simulation.periodCount = 2
        simulation.numberOfIterations = 1000
        simulation.randomSeed = 123
        simulation.comment = "comment"
        simulation.save()

        List<SimulationInfo> infos = resultService.getSimulationInfos(parameterization1.id)
        assertEquals 1, infos.size()
        SimulationInfo info = infos[0]

        assertEquals resultConfiguration.name, info.resultTemplateName
        assertEquals simulation.name, info.name
        assertEquals simulation.numberOfIterations, info.iterationCount
        assertEquals simulation.comment, info.comment
        assertEquals simulation.id, info.simulationId
        assertEquals simulation.randomSeed, info.randomSeed
    }

    void testGetResults() {
        Parameterization parameterization1 = new Parameterization("test1")
        parameterization1.modelClass = EmptyModel
        parameterization1.dealId = 1
        parameterization1.periodCount = 1
        SimpleDateFormat dateFormat = new SimpleDateFormat(Parameterization.PERIOD_DATE_FORMAT)
        Date first = new GregorianCalendar(2010, 0, 1).time
        Date second = new GregorianCalendar(2011, 0, 1).time
        parameterization1.periodLabels = [dateFormat.format(first), dateFormat.format(second)]
        parameterization1.save()

        ResultConfiguration resultConfiguration = new ResultConfiguration("rc1")
        resultConfiguration.modelClass = EmptyModel
        resultConfiguration.save()

        Simulation simulation = new Simulation("s1")
        simulation.modelClass = EmptyModel
        simulation.parameterization = parameterization1
        simulation.template = resultConfiguration
        simulation.periodCount = 2
        simulation.numberOfIterations = 1000
        simulation.randomSeed = 123
        simulation.comment = "comment"
        simulation.save()

        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 0, path: path1, field: field, collector: collector1, valueIndex: 0, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path1, field: field, collector: collector1, valueIndex: 0, value: 20).save()


        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 0, path: path2, field: field, collector: collector1, valueIndex: 0, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path2, field: field, collector: collector1, valueIndex: 0, value: 20).save()

        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 1, iteration: 0, path: path1, field: field, collector: collector1, valueIndex: 0, value: 100).save()
        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 1, iteration: 1, path: path1, field: field, collector: collector1, valueIndex: 0, value: 200).save()

        String combinedPath = path1.pathName + ":" + field.fieldName
        List<ResultInfo> results = resultService.getResults(simulation.id, [combinedPath])
        assertEquals 2, results.size()

        ResultInfo resultInfo = results[0]
        assertEquals 0, resultInfo.periodIndex
        assertEquals combinedPath, resultInfo.path
        assertEquals first, resultInfo.periodDate

        assertEquals 2, resultInfo.values.size()
        assertEquals 0, resultInfo.values[0].iteration
        assertEquals 10, resultInfo.values[0].value
        assertEquals 1, resultInfo.values[1].iteration
        assertEquals 20, resultInfo.values[1].value

        resultInfo = results[1]
        assertEquals 1, resultInfo.periodIndex
        assertEquals combinedPath, resultInfo.path
        assertEquals second, resultInfo.periodDate

        assertEquals 2, resultInfo.values.size()
        assertEquals 0, resultInfo.values[0].iteration
        assertEquals 100, resultInfo.values[0].value
        assertEquals 1, resultInfo.values[1].iteration
        assertEquals 200, resultInfo.values[1].value
    }
}
