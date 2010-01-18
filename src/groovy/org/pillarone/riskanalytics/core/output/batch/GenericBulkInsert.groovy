package org.pillarone.riskanalytics.core.output.batch

import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.pillarone.riskanalytics.core.output.PathMapping
import org.pillarone.riskanalytics.core.output.FieldMapping
import org.pillarone.riskanalytics.core.output.CollectorMapping

class GenericBulkInsert extends AbstractBulkInsert {

    protected void save() {
        tempFile.eachLine { String line ->
            String[] values = line.split(",")
            SingleValueResult result = new SingleValueResult()
            result.simulationRun = simulationRun
            result.period = Integer.parseInt(values[1])
            result.iteration = Integer.parseInt(values[2])
            result.path = PathMapping.get(Long.parseLong(values[3]))
            result.field = FieldMapping.get(Long.parseLong(values[4]))
            result.collector = CollectorMapping.get(Long.parseLong(values[5]))
            result.value = Double.parseDouble(values[6])
            result.valueIndex = 0
            result.save()
        }
    }

    protected void writeResult(List values) {
        writer.writeLine(values.join(","))
    }


}