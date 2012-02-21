package org.pillarone.riskanalytics.core.remoting.impl

import java.text.SimpleDateFormat
import models.core.CoreModel
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ParameterizationInfo
import org.pillarone.riskanalytics.core.remoting.ResultInfo
import org.pillarone.riskanalytics.core.remoting.SimulationInfo
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation
import org.pillarone.riskanalytics.core.output.*
import org.pillarone.riskanalytics.core.workflow.Status
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.simulation.item.parameter.DateParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

class ResultServiceTests extends GroovyTestCase {

    IResultService resultService

    PathMapping path1
    PathMapping path2
    PathMapping path3
    FieldMapping field
    CollectorMapping collector1
    String path1Name = "Artisan:grossClaims1:subFire:outClaimsShort"
    String path2Name = "Artisan:grossClaims1:subMotor:outClaimsShort"
    String path1RegExName = "Artisan:grossClaims1:(.*):outClaimsShort"
    String path3Name = "Artisan:grossClaims2:subsubcomponents:outClaimsShort2"
    String path2RegExName = "Artisan:grossClaims2:(.*):outClaimsShort2"
    // no path matching to this regex
    String path3RegExName = "Artisan:grossClaims20:(.*):outClaimsShort2"

    void setUp() {
        path1 = PathMapping.findByPathName(path1Name)
        if (path1 == null) {
            path1 = new PathMapping(pathName: path1Name).save()
        }

        path2 = PathMapping.findByPathName(path2Name)
        if (path2 == null) {
            path2 = new PathMapping(pathName: path2Name).save()
        }

        path3 = PathMapping.findByPathName(path3Name)
        if (path3 == null) {
            path3 = new PathMapping(pathName: path3Name).save()
        }

        field = FieldMapping.findByFieldName('value')
        if (field == null) {
            field = new FieldMapping(fieldName: 'value').save()
        }

        collector1 = CollectorMapping.findByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)
        if (collector1 == null) {
            collector1 = new CollectorMapping(collectorName: SingleValueCollectingModeStrategy.IDENTIFIER).save()
        }

    }


    void testGetParamInfos() {
        Parameterization parameterization1 = new Parameterization("test1")
        parameterization1.modelClass = CoreModel
        parameterization1.dealId = 1
        parameterization1.periodCount = 1
        parameterization1.comment = "comment 1"
        parameterization1.status = Status.IN_REVIEW
        parameterization1.valuationDate = new DateTime(2010, 1, 1, 0, 0, 0, 0)
        parameterization1.save()

        Parameterization parameterization2 = new Parameterization("test2")
        parameterization2.modelClass = CoreModel
        parameterization2.dealId = 1
        parameterization2.periodCount = 1
        parameterization2.comment = "comment 2"
        parameterization2.status = Status.IN_PRODUCTION
        parameterization2.valuationDate = new DateTime(2011, 1, 1, 0, 0, 0, 0)
        parameterization2.save()

        Parameterization parameterization3 = new Parameterization("test2")
        parameterization3.modelClass = CoreModel
        parameterization3.dealId = 2
        parameterization3.periodCount = 1
        parameterization3.save()

        List<ParameterizationInfo> infos = resultService.getParameterizationInfosForTransactionId(1)

        assertEquals 2, infos.size()
        ParameterizationInfo info = infos.find { it.parameterizationId == parameterization1.id}
        assertEquals parameterization1.name, info.name
        assertEquals parameterization1.comment, info.comment
        assertEquals parameterization1.versionNumber.toString(), info.version
        assertEquals parameterization1.status, info.status

        info = infos.find { it.parameterizationId == parameterization2.id}
        assertEquals parameterization2.name, info.name
        assertEquals parameterization2.comment, info.comment
        assertEquals parameterization2.versionNumber.toString(), info.version
        assertEquals parameterization2.status, info.status
    }

    void testGetSimulationInfos() {
        FileImportService.importModelsIfNeeded([CoreModel.getSimpleName() - "Model"])

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
        simulation.addParameter(ParameterHolderFactory.getHolder("runtimeUpdateDate", 0, new DateTime()))

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

        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 0, path: path3, field: field, collector: collector1, valueIndex: 0, value: 10).save()
        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path3, field: field, collector: collector1, valueIndex: 0, value: 20).save()

        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 1, iteration: 0, path: path1, field: field, collector: collector1, valueIndex: 0, value: 100).save()
        assertNotNull new SingleValueResult(simulationRun: simulation.simulationRun, period: 1, iteration: 1, path: path1, field: field, collector: collector1, valueIndex: 0, value: 200).save()

        assertNotNull new PostSimulationCalculation(run: simulation.simulationRun, keyFigure: PostSimulationCalculation.MEAN, collector: collector1, path: path1, field: field, period: 0, result: 5).save()
        assertNotNull new PostSimulationCalculation(run: simulation.simulationRun, keyFigure: PostSimulationCalculation.MEAN, collector: collector1, path: path1, field: field, period: 0, result: 10).save()

        assertNotNull new PostSimulationCalculation(run: simulation.simulationRun, keyFigure: PostSimulationCalculation.MEAN, collector: collector1, path: path2, field: field, period: 0, result: 5).save()
        assertNotNull new PostSimulationCalculation(run: simulation.simulationRun, keyFigure: PostSimulationCalculation.MEAN, collector: collector1, path: path2, field: field, period: 0, result: 20).save()

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
        assertEquals first, resultInfo.values[0].periodDate

        assertEquals 1, resultInfo.values[1].iteration
        assertEquals 20, resultInfo.values[1].value
        assertEquals first, resultInfo.values[1].periodDate

        resultInfo = results[1]
        assertEquals 1, resultInfo.periodIndex
        assertEquals combinedPath, resultInfo.path
        assertEquals second, resultInfo.periodDate

        assertEquals 2, resultInfo.values.size()
        assertEquals 0, resultInfo.values[0].iteration
        assertEquals 100, resultInfo.values[0].value
        assertEquals second, resultInfo.values[0].periodDate

        assertEquals 1, resultInfo.values[1].iteration
        assertEquals 200, resultInfo.values[1].value
        assertEquals second, resultInfo.values[1].periodDate

        String regExcombinedPath = path1RegExName + ":" + field.fieldName
        List<ResultInfo> resultsWithRegEx = resultService.getResults(simulation.id, [regExcombinedPath])
        assertEquals 3, resultsWithRegEx.size()

        String combinedName1 = path1.pathName + ":" + field.fieldName
        String combinedName2 = path2.pathName + ":" + field.fieldName
        assertEquals 2, resultsWithRegEx.findAll {it.path == combinedName1}.size()
        assertEquals 1, resultsWithRegEx.findAll {it.path == combinedName2}.size()

        String notMatched = path3RegExName + ":" + field.fieldName
        resultsWithRegEx = resultService.getResults(simulation.id, [notMatched])
        assertEquals 0, resultsWithRegEx.size()

    }
}
