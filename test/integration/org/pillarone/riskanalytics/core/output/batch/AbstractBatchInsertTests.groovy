package org.pillarone.riskanalytics.core.output.batch

import grails.test.GrailsUnitTestCase
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AbstractBatchInsertTests extends GrailsUnitTestCase {

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
        switchEnvironment Environment.DEVELOPMENT

        assertEquals "GenericBatchInsert expected in environment ${Environment.getCurrent().name}", GenericBulkInsert, AbstractBulkInsert.getBulkInsertInstance().class

        switchEnvironment Environment.TEST
        assertEquals "GenericBatchInsert expected in environment ${Environment.getCurrent().name}", GenericBulkInsert, AbstractBulkInsert.getBulkInsertInstance().class

        switchEnvironment "mysql"
        assertEquals "MysqlBatchInsert expected in environment ${Environment.getCurrent().name}", MysqlBulkInsert, AbstractBulkInsert.getBulkInsertInstance().class

        switchEnvironment "standalone"
        assertEquals "DerbyBatchInsert expected in environment ${Environment.getCurrent().name}", DerbyBulkInsert, AbstractBulkInsert.getBulkInsertInstance().class
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