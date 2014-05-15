package org.pillarone.riskanalytics.core.simulation.engine

import models.core.CoreModel
import org.junit.After
import org.pillarone.riskanalytics.core.dataaccess.ResultAccessor
import org.pillarone.riskanalytics.core.packets.AggregatedExternalPacket
import org.pillarone.riskanalytics.core.packets.SingleExternalPacket

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.pillarone.riskanalytics.core.packets.ExternalPacket
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.ResultConfiguration
import org.pillarone.riskanalytics.core.simulation.item.Simulation


class ResultDataTests {

    PathMapping path
    FieldMapping field1
    FieldMapping field2
    CollectorMapping aggregated
    CollectorMapping single

    Simulation simulation

    @Before
    void init() {
        FileImportService.importModelsIfNeeded(['Core'])

        Parameterization parameterization = new Parameterization("CoreParameters", CoreModel)
        parameterization.load()

        ResultConfiguration resultConfiguration = new ResultConfiguration("CoreResultConfiguration", CoreModel)
        resultConfiguration.load()

        simulation = new Simulation("Test")
        simulation.parameterization = parameterization
        simulation.template = resultConfiguration
        simulation.modelClass = CoreModel
        simulation.numberOfIterations = 3
        simulation.periodCount = 1
        simulation.randomSeed = 1

        simulation.save()

        path = PathMapping.findOrSaveByPathName("test-path")
        field1 = FieldMapping.findOrSaveByFieldName("field-1")
        field2 = FieldMapping.findOrSaveByFieldName("field-2")

        aggregated = CollectorMapping.findOrSaveByCollectorName(AggregatedCollectingModeStrategy.IDENTIFIER)
        single = CollectorMapping.findOrSaveByCollectorName(SingleValueCollectingModeStrategy.IDENTIFIER)

    }

    @After
    void cleanUp() {
        simulation.delete()
        ResultAccessor.clearCaches()
    }

    private void writeResult(ResultWriter resultWriter, SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);
        dos.writeDouble(0);

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.collector.id, result.period), null, bos.toByteArray(), 0));
    }

    private void writeResult(ResultWriter resultWriter, List<SingleValueResult> result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result[0].iteration);
        dos.writeInt(result.size());
        result.each {
            dos.writeDouble(it.value);
            dos.writeDouble(0);
        }

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result[0].field.id, result[0].path.id, result[0].collector.id, result[0].period), null, bos.toByteArray(), 0));
    }

    @Test
    void testLoadAggregated() {

        ResultWriter writer = new ResultWriter(simulation.id)
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path, collector: aggregated, field: field1, value: 100))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 2, path: path, collector: aggregated, field: field1, value: 200))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 3, path: path, collector: aggregated, field: field1, value: 300))

        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path, collector: aggregated, field: field2, value: 1000))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 2, path: path, collector: aggregated, field: field2, value: 2000))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 3, path: path, collector: aggregated, field: field2, value: 3000))


        ResultData resultData = new ResultData()

        DataSourceDefinition definition = new DataSourceDefinition("CoreParameters", "1", CoreModel, path.pathName, [field1.fieldName, field2.fieldName],[0], aggregated.collectorName)

        resultData.load([definition])

        List<ExternalPacket> values = resultData.getValuesForDefinition(definition)
        assertEquals(3, values.size())

        AggregatedExternalPacket value = values.find { it.iteration == 1 }
        assertEquals(100, value.getValue(field1.fieldName), 0)
        assertEquals(1000, value.getValue(field2.fieldName), 0)

        value = values.find { it.iteration == 2 }
        assertEquals(200, value.getValue(field1.fieldName), 0)
        assertEquals(2000, value.getValue(field2.fieldName), 0)

        value = values.find { it.iteration == 3 }
        assertEquals(300, value.getValue(field1.fieldName), 0)
        assertEquals(3000, value.getValue(field2.fieldName), 0)

    }

    @Test
    void testLoadSingle() {

        ResultWriter writer = new ResultWriter(simulation.id)
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path, collector: single, field: field1, value: 100, valueIndex: 0))
        writeResult(writer, [new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 2, path: path, collector: single, field: field1, value: 200, valueIndex: 0), new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 2, path: path, collector: single, field: field1, value: 300, valueIndex: 0)])
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 3, path: path, collector: single, field: field1, value: 300, valueIndex: 0))

        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 1, path: path, collector: single, field: field2, value: 1000, valueIndex: 0))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 2, path: path, collector: single, field: field2, value: 2000, valueIndex: 0))
        writeResult(writer, new SingleValueResult(simulationRun: simulation.simulationRun, period: 0, iteration: 3, path: path, collector: single, field: field2, value: 3000, valueIndex: 0))


        ResultData resultData = new ResultData()

        DataSourceDefinition definition = new DataSourceDefinition("CoreParameters", "1", CoreModel, path.pathName, [field1.fieldName, field2.fieldName],[0], single.collectorName)

        resultData.load([definition])

        List<ExternalPacket> values = resultData.getValuesForDefinition(definition)
        assertEquals(3, values.size())

        SingleExternalPacket value = values.find { it.iteration == 1 }
        assertEquals(100, value.getValues(field1.fieldName)[0].aDouble, 0)
        assertEquals(1000, value.getValues(field2.fieldName)[0].aDouble, 0)

        value = values.find { it.iteration == 2 }
        assertEquals(200, value.getValues(field1.fieldName)[0].aDouble, 0)
        assertEquals(300, value.getValues(field1.fieldName)[1].aDouble, 0)
        assertEquals(2000, value.getValues(field2.fieldName)[0].aDouble, 0)

        value = values.find { it.iteration == 3 }
        assertEquals(300, value.getValues(field1.fieldName)[0].aDouble, 0)
        assertEquals(3000, value.getValues(field2.fieldName)[0].aDouble, 0)

    }
}
