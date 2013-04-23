package org.pillarone.riskanalytics.core.output.batch.calculations

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.output.PostSimulationCalculation
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.SimulationRun

class GenericBulkInsert extends AbstractCalculationsBulkInsert {

    protected void save() {
        SimulationRun.withTransaction { status ->
            tempFile.eachLine {String line ->
                String[] values = line.split(",")
                PostSimulationCalculation result = new PostSimulationCalculation()
                result.run = simulationRun
                result.period = Integer.parseInt(values[1])
                result.path = PathMapping.get(Long.parseLong(values[2]))
                result.field = FieldMapping.get(Long.parseLong(values[3]))
                result.collector = CollectorMapping.get(Long.parseLong(values[4]))
                result.keyFigure = values[5]
                result.keyFigureParameter = values[6] != "null" ? Double.parseDouble(values[6]) : null
                result.result = Double.parseDouble(values[7])
                if(!result.save()) {
                    println result.errors
                }
            }
        }
    }

    @CompileStatic
    protected void writeResult(List values) {
        writer.writeLine(values.join(","))
    }

    @CompileStatic
    protected String getNull() {
        return "null"
    }


}
