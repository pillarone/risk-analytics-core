package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestParameterObjectType extends AbstractParameterObjectClassifier {

    public static final TestParameterObjectType NESTED_CONSTRAINTS_TABLE = new TestParameterObjectType(
            "nested constraints table", "NESTED_CONSTRAINTS_TABLE", [
            constraintTable: TestConstraintsTableType.getStrategy(TestConstraintsTableType.THREE_COLUMNS,
                    ['table': TestConstrainedTable.getDefault()]),
            mode: ResultViewMode.INCREMENTAL])

    public static final all = [NESTED_CONSTRAINTS_TABLE]
    protected static Map types = [:]
    static {
        TestParameterObjectType.all.each {
            TestParameterObjectType.types[ it.toString() ] = it
        }
    }

    private TestParameterObjectType(String displayName, String typeName, Map parameters) {
        super(displayName, typeName, parameters)
    }

    public static TestParameterObjectType valueOf(String type) {
        types[type]
    }

    List<IParameterObjectClassifier> getClassifiers() {
        all
    }

    IParameterObject getParameterObject(Map parameters) {
        TestParameterObjectType.getStrategy(this, parameters)
    }

    static IParameterObject getDefault() {
        new TestConstrainedTableStrategy(table : TestConstrainedTable.getDefault())
    }

    static IParameterObject getStrategy(TestParameterObjectType type, Map parameters) {
        new TestConstrainedTableStrategy(table : (ConstrainedMultiDimensionalParameter) parameters['table'],
                                         mode : parameters['mode'])
    }
}
