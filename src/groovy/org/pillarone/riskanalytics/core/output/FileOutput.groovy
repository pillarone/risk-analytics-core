package org.pillarone.riskanalytics.core.output

import org.pillarone.riskanalytics.core.simulation.engine.SimulationScope

public class FileOutput implements ICollectorOutputStrategy {

    String resultLocation
    SimulationScope simulationScope
    String filename = null;

    public ICollectorOutputStrategy leftShift(List results) {

        File resultFile = new File(getFileName(results[0]))
        if (!resultFile.exists()) {
            resultFile.withWriter {writer ->
                writer.writeLine("iteration\tperiod\tvalueIndex\tpath\tfield\tvalue")
            }
        }

        resultFile.withWriterAppend {writer ->
            for (SingleValueResultPOJO result in results) {
                writer.writeLine("${result.iteration}\t${result.period}\t${result.valueIndex}\t${result.path.pathName}\t${result.field.fieldName}\t${result.value}")
            }
        }

        return this
    }

    private String getFileName(SingleValueResultPOJO result) {
        if (filename == null)
            filename = "${resultLocation}${File.separator}${replaceChars(simulationScope.simulation.name)}.tsl"
        return filename;
    }

    private String replaceChars(String s) {
        char replacement = "_" as char

        s = s.replace(":" as char, replacement)
        s = s.replace("\\" as char, replacement)
        s = s.replace("/" as char, replacement)
        s = s.replace("*" as char, replacement)
        s = s.replace("?" as char, replacement)
        s = s.replace("\"" as char, replacement)
        s = s.replace("<" as char, replacement)
        s = s.replace(">" as char, replacement)
        s = s.replace("|" as char, replacement)

        return s
    }

    public void finish() {

    }


}
