package models.dependingCore

import models.core.CoreModel
import org.pillarone.riskanalytics.core.components.DataSourceDefinition
import org.pillarone.riskanalytics.core.example.migration.TestConstraintsTableType
import org.pillarone.riskanalytics.core.example.migration.TimeMode
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory
import org.pillarone.riskanalytics.core.util.GroovyUtils

model = DependingCoreModel
periodCount = 1
displayName = 'DependingCoreParameters'
applicationVersion = '1.3'
periodLabels = ["2011-03-30", "2011-03-30", "2011-03-30"]
components {
    dependingComponent {
        parmDefinition[0] = new DataSourceDefinition("CoreParameters", "1", CoreModel, "outPath",["x","y"],[0], AggregatedCollectingModeStrategy.IDENTIFIER)
    }
    exampleInputOutputComponent {
        parmNewParameterObject[0] = ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.TYPE0, ["b": 1.0, "a": 0.0,])
        parmParameterObject[0] = ExampleParameterObjectClassifier.getStrategy(ExampleParameterObjectClassifier.TYPE0, ["b": 1.0, "a": 0.0,])
    }
}
comments = []
