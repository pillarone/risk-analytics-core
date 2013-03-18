package org.pillarone.riskanalytics.core.output

import grails.test.GrailsUnitTestCase
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.pillarone.riskanalytics.core.output.FileOutput
import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope
import org.pillarone.riskanalytics.core.simulation.item.Simulation

class FileOutputTests extends GrailsUnitTestCase {

    File file

    protected void setUp() {
        super.setUp()
        file = new File("newRun.tsl")
    }

    protected void tearDown() {
        file.delete()
    }

    void testCreateNewFile() {

        SingleValueResultPOJO result = new SingleValueResultPOJO()
        result.simulationRun = new SimulationRun(name: "newRun")
        result.path = new PathMapping(pathName: "path")
        result.field = new FieldMapping(fieldName: "field")


        assertFalse "File does already exist. Fix setup", file.exists()

        FileOutput output = new FileOutput(resultLocation: ".", simulationScope: new SimulationScope(simulation: new Simulation(result.simulationRun.name)))

        output << [result]

        assertTrue "file not created", file.exists()

    }

    void testCreateNewFileWithInvalidChars() {

        SingleValueResultPOJO result = new SingleValueResultPOJO()
        result.simulationRun = new SimulationRun(name: "new?Run:")
        result.path = new PathMapping(pathName: "path")
        result.field = new FieldMapping(fieldName: "field")

        file = new File("new_Run_.tsl")
        assertFalse "File does already exist. Fix setup", file.exists()

        FileOutput output = new FileOutput(resultLocation: ".", simulationScope: new SimulationScope(simulation: new Simulation(result.simulationRun.name)))

        output << [result]

        assertTrue "file not created", file.exists()

    }

    void testAppendToFile() {
        SingleValueResultPOJO result = new SingleValueResultPOJO()
        result.simulationRun = new SimulationRun(name: "newRun")
        result.path = new PathMapping(pathName: "path")
        result.field = new FieldMapping(fieldName: "field")


        FileOutput output = new FileOutput(resultLocation: ".", simulationScope: new SimulationScope(simulation: new Simulation(result.simulationRun.name)))

        output << [result]
        assertEquals 2, lineCount
        output << [result]
        assertEquals 3, lineCount


    }

    private def getLineCount() {
        int lineCount = 0
        file.eachLine {
            lineCount++
        }
        return lineCount
    }

}
