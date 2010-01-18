package models.core

import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy

model = models.core.CoreModel
periodCount = 1

components {
    dynamicComponent {
        subSubcomponent {
            outValue = AggregatedCollectingModeStrategy.IDENTIFIER
        }
    }
    exampleInputOutputComponent {
        outValue = AggregatedCollectingModeStrategy.IDENTIFIER
    }
    exampleOutputComponent {
        outValue1 = SingleValueCollectingModeStrategy.IDENTIFIER
        outValue2 = SingleValueCollectingModeStrategy.IDENTIFIER
    }
}
