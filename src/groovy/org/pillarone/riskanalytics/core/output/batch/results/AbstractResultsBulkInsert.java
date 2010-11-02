package org.pillarone.riskanalytics.core.output.batch.results;

import groovy.util.ConfigObject;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pillarone.riskanalytics.core.output.SingleValueResultPOJO;
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert;
import org.pillarone.riskanalytics.core.util.GroovyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/*
 * This is class is in java because it is called very often during a simulation and there is a significant performance boost compared to groovy.
 */
public abstract class AbstractResultsBulkInsert extends AbstractBulkInsert {

    void addResults(List<SingleValueResultPOJO> results) {
        List values = new ArrayList(7);
        for (SingleValueResultPOJO result : results) {
            values.add(getSimulationRunId());
            values.add(result.getPeriod());
            values.add(result.getIteration());
            values.add(GroovyUtils.getId(result.getPath()));
            values.add(GroovyUtils.getId(result.getField()));
            values.add(GroovyUtils.getId(result.getCollector()));
            values.add(result.getValue());
            values.add(result.getValueIndex());
            if (result.getDate() == null) {
                values.add(null);
            }
            else {
                values.add(new SimpleDateFormat("yyyy-MM-dd").format(result.getDate()));
            }
            writeResult(values);
            values.clear();
        }
    }

    public static AbstractBulkInsert getBulkInsertInstance() {
        try {
            ConfigObject configObject = ApplicationHolder.getApplication().getConfig();
            Class bulkClass = null;
            if (configObject.containsKey("resultBulkInsert")) {
                bulkClass = (Class) configObject.get("resultBulkInsert");
            }
            if (bulkClass == null) {
                return new GenericBulkInsert();
            }
            return (AbstractBulkInsert) AbstractBulkInsert.class.getClassLoader().loadClass(bulkClass.getName()).newInstance();
        } catch (Exception e) {
            return new GenericBulkInsert();
        }
    }
}
