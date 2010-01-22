package org.pillarone.riskanalytics.core.output.batch


public class MysqlBatchInsertTests extends GroovyTestCase {
    void testGetPathId() {
        MysqlBulkInsert batchInsert = new MysqlBulkInsert()

        String path = "TEST_PATH"
        int id = batchInsert.getPathId(path)
        assertEquals id, batchInsert.getPathId(path)
        batchInsert.pathIds = [:]
        assertEquals id, batchInsert.getPathId(path)
    }

    void testGetFieldId() {
        MysqlBulkInsert batchInsert = new MysqlBulkInsert()

        String field = "TEST_FIELD"
        int id = batchInsert.getFieldId(field)
        assertEquals id, batchInsert.getFieldId(field)
        batchInsert.fieldIds = [:]
        assertEquals id, batchInsert.getFieldId(field)
    }


    void testGetCollectorId() {
        MysqlBulkInsert batchInsert = new MysqlBulkInsert()

        String collectorName = "TEST_Collector"
        int id = batchInsert.getCollectorId(collectorName)
        assertEquals id, batchInsert.getCollectorId(collectorName)
        batchInsert.collectorIds = [:]
        assertEquals id, batchInsert.getCollectorId(collectorName)
    }
    
}