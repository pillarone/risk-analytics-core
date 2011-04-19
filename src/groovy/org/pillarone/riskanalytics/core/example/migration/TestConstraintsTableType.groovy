package org.pillarone.riskanalytics.core.example.migration

import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class TestConstraintsTableType extends AbstractParameterObjectClassifier {

    public static final TestConstraintsTableType TWO_COLUMNS = new TestConstraintsTableType('two columns', 'TWO_COLUMNS',
        [table : TestConstrainedTable.getDefault()]
    )

    public static final all = [TWO_COLUMNS]

    protected static Map types = [:]

    static {
        TestConstraintsTableType.all.each {
            TestConstraintsTableType.types[ it.toString() ] = it
        }
    }

    private TestConstraintsTableType(String displayName, String typeName, Map parameters) {
        super(displayName, typeName, parameters)
    }

    public static TestConstraintsTableType valueOf(String type) {
        types[type]
    }

    List<IParameterObjectClassifier> getClassifiers() {
       all
    }

    IParameterObject getParameterObject(Map parameters) {
        getStrategy(this, parameters)
    }

    static IParameterObject getStrategy(TestConstraintsTableType type, Map parameters) {
        new TestConstrainedTableStrategy(table: (ConstrainedMultiDimensionalParameter) parameters['table'])
    }
}
