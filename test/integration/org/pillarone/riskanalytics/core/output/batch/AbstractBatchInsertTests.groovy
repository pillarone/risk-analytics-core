package org.pillarone.riskanalytics.core.output.batch

import grails.util.Environment
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pillarone.riskanalytics.core.output.batch.results.AbstractResultsBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.GenericBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.MysqlBulkInsert

class AbstractBatchInsertTests extends GroovyTestCase {

    Environment env

    protected void setUp() {
        super.setUp()
        env = Environment.getCurrent()
    }

    protected void tearDown() {
        switchEnvironment env
        super.tearDown()
    }



    void testFactoryMethod() {
        //TODO
//        switchEnvironment Environment.DEVELOPMENT
//
//        assertEquals "GenericBatchInsert expected in environment ${Environment.getCurrent().name}", GenericBulkInsert, AbstractResultsBulkInsert.getBulkInsertInstance().class
//
//        switchEnvironment Environment.TEST
//        assertEquals "GenericBatchInsert expected in environment ${Environment.getCurrent().name}", GenericBulkInsert, AbstractResultsBulkInsert.getBulkInsertInstance().class
//
//        switchEnvironment "mysql"
//        assertEquals "MysqlBatchInsert expected in environment ${Environment.getCurrent().name}", MysqlBulkInsert, AbstractResultsBulkInsert.getBulkInsertInstance().class

    }

    private def switchEnvironment(Environment env) {
        System.setProperty(Environment.KEY, env.name())
        ConfigurationHolder.config = null
        assertEquals "Expected env", env, Environment.getCurrent()
    }

    private def switchEnvironment(String envName) {
        System.setProperty(Environment.KEY, envName)
        ConfigurationHolder.config = null
        assertEquals "Expected env", Environment.CUSTOM, Environment.getCurrent()
        assertEquals "Expected env name", envName, Environment.getCurrent().name
    }
}