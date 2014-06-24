package org.pillarone.riskanalytics.core.test

import org.pillarone.riskanalytics.core.output.SingleValueResult
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultDescriptor
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultTransferObject
import org.pillarone.riskanalytics.core.simulation.engine.grid.output.ResultWriter


abstract class TestUtils {

    public static void writeResult(ResultWriter resultWriter, SingleValueResult result) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result.iteration);
        dos.writeInt(1);
        dos.writeDouble(result.value);
        dos.writeLong(0);
        dos.writeUTF("")

        resultWriter.writeResult(new ResultTransferObject(new ResultDescriptor(result.field.id, result.path.id, result.collector.id, result.period), null, bos.toByteArray(), 0));

    }
}
