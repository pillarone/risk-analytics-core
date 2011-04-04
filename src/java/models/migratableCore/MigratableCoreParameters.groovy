package models.migratableCore

model=models.migratableCore.MigratableCoreModel
periodCount=1
displayName='MigratableCoreParameters'
applicationVersion='1.3'
periodLabels=["2011-03-30","2011-03-30","2011-03-30"]
components {
	composite {
		parmStrategy[0]=org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table":new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], /*["Motor", "Engine"],*/ [100.0, 80.0]]),["id",/*"type",*/"value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')),/*"mode":org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,*/])
//		parmTimeMode[0]=org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD
	}
	dynamic {
		subOpt1 {
			parmStrategy[0]=org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.getStrategy(org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType.THREE_COLUMNS, ["table":new org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter(org.pillarone.riskanalytics.core.util.GroovyUtils.toList([["S1", "S2"], /*["Motor", "Engine"],*/ [100.0, 80.0]]),["id",/*"type",*/"value"], org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory.getConstraints('constrained table')),/*"mode":org.pillarone.riskanalytics.core.example.migration.ResultViewMode.INCREMENTAL,*/])
			parmTimeMode[0]=org.pillarone.riskanalytics.core.example.migration.TimeMode.PERIOD
		}
	}
	exampleInputOutputComponent {
		parmNewParameterObject[0]=org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.getStrategy(org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.TYPE0, ["b":1.0,"a":0.0,])
		parmParameterObject[0]=org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.getStrategy(org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier.TYPE0, ["b":1.0,"a":0.0,])
	}
}
comments=[]
