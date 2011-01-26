package org.pillarone.riskanalytics.core.dataaccess;

import org.pillarone.riskanalytics.core.output.SimulationRun;
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IterationFileAccessor {
    DataInputStream dis;
    int id;
    double value;

    public IterationFileAccessor(File f) throws Exception {

        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bs = new BufferedInputStream(fis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] b = new byte[8048];
        int len;
        int count = 0;
        while ((len = bs.read(b)) != -1) {
            bos.write(b, 0, len);
            count++;
        }
        if (count == 0) {
            Thread.sleep(2000);
            while ((len = bs.read(b)) != -1) {
                bos.write(b, 0, len);
                count++;
            }
        }
        bs.close();
        fis.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        dis = new DataInputStream(bis);

    }

    public boolean fetchNext() throws Exception {
        if (dis.available() > 4) {
            id = dis.readInt();
            int len = dis.readInt();
            value = 0;
            for (int i = 0; i < len; i++) {
                value += dis.readDouble();
                dis.readLong();
            }

            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public static List getValuesSorted(Long runId, int period, long pathId, long collectorId, long fieldId) throws Exception {
        File iterationFile = new File(GridHelper.getResultLocation(runId) + File.separator + pathId + "_" + period + "_" + fieldId);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        List<Double> values = new ArrayList<Double>();
        while (ifa.fetchNext()) {
            values.add(ifa.getValue());
        }
        Collections.sort(values);
        return values;
    }
}
