package org.pillarone.riskanalytics.core.output.batch.results;

import grails.util.Holders;
import groovy.transform.CompileStatic;
import groovy.util.ConfigObject;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO;
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert;
import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This is class is in java because it is called very often during a simulation and there is a significant performance boost compared to groovy.
 */
public abstract class AbstractResultsBulkInsert extends AbstractBulkInsert {

    private Map<Object, Long> idCache = new HashMap<Object, Long>();

    @CompileStatic
    void addResults(List<SingleValueResultPOJO> results) {
        List values = new ArrayList(7);
        for (SingleValueResultPOJO result : results) {
            values.add(Long.toString(getSimulationRunId()));
            values.add(Integer.toString(result.getPeriod()));
            values.add(Integer.toString(result.getIteration()));
            values.add(Long.toString(obtainId(result.getPath())));
            values.add(Long.toString(obtainId(result.getField())));
            values.add(Long.toString(obtainId(result.getCollector())));
            values.add(Double.toString(result.getValue()));
            values.add(Integer.toString(result.getValueIndex()));
            if (result.getDate() == null) {
                values.add(null);
            }
            else {
                values.add(Long.toString(result.getDate().getMillis()));
            }
            writeResult(values);
            values.clear();
        }
    }

    @CompileStatic
    private long obtainId(Object mapping) {
        if(idCache.containsKey(mapping)) {
            return idCache.get(mapping);
        }

        long id = GroovyUtils.getId(mapping);
        idCache.put(mapping, id);

        return id;
    }

    public static AbstractResultsBulkInsert getBulkInsertInstance() {
        try {
            ConfigObject configObject = Holders.getGrailsApplication().getConfig();
            Class bulkClass = null;
            if (configObject.containsKey("resultBulkInsert")) {
                bulkClass = (Class) configObject.get("resultBulkInsert");
            }
            if (bulkClass == null) {
                return new GenericBulkInsert();
            }
            return (AbstractResultsBulkInsert) Thread.currentThread().getContextClassLoader().loadClass(bulkClass.getName()).newInstance();
        } catch (Exception e) {
            return new GenericBulkInsert();
        }
    }
}
