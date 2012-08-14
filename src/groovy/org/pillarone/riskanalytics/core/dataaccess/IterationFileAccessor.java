package org.pillarone.riskanalytics.core.dataaccess;

import org.joda.time.DateTime;
import org.pillarone.riskanalytics.core.simulation.engine.grid.GridHelper;

import java.io.*;
import java.util.*;

public class IterationFileAccessor {
    protected DataInputStream dis;
    protected int iteration;
    protected List<DateTimeValuePair> value;

    public IterationFileAccessor(File f) throws Exception {
        if (f.exists()) {
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

    }

    public boolean fetchNext() throws Exception {
        if (dis != null && dis.available() > 4) {
            iteration = dis.readInt();
            int len = dis.readInt();
            value = new ArrayList<DateTimeValuePair>(len);
            for (int i = 0; i < len; i++) {
                final double doubleValue = dis.readDouble();
                final long dateTimeLong = dis.readLong();
                DateTimeValuePair dateTimeValuePair = new DateTimeValuePair( dateTimeLong , doubleValue);
                value.add(dateTimeValuePair);
            }
            return true;
        }
        return false;
    }

    public int getIteration() {
        return iteration;
    }

    public double getValue() {
        double result = 0;
        for (DateTimeValuePair d : value) {
            result += d.aDouble;
        }
        return result;
    }

    public List<DateTimeValuePair> getSingleValues() {
        return value;
    }

    public static List getValuesSorted(Long runId, int period, long pathId, long collectorId, long fieldId) throws Exception {
        File iterationFile = new File(GridHelper.getResultPathLocation(runId, pathId, fieldId, collectorId, period));
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);
        List<Double> values = new ArrayList<Double>();
        while (ifa.fetchNext()) {
            values.add(ifa.getValue());
        }
        Collections.sort(values);
        return values;
    }

    public static Map<Integer, Double> getIterationConstrainedValues(long runId, int period, long path, long field, long collector,
                                                                     Collection<Integer> iterations) throws Exception {
        File iterationFile = new File(GridHelper.getResultPathLocation(runId, path, field, collector, period));
        HashMap<Integer, Double> values = new HashMap<Integer, Double>(10000);
        IterationFileAccessor ifa = new IterationFileAccessor(iterationFile);

        while (ifa.fetchNext()) {
            int iteration = ifa.getIteration();
            if (iterations.contains(iteration)) {
                values.put(iteration, ifa.getValue());
            }
        }
        return values;
    }
}
