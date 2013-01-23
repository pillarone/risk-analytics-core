package models.core.parameterApplicator

import org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker


model = ParameterApplicatorModel
periodCount = 1

components {
    input {
        parmMultiDimensionalParameter[0] = new SimpleMultiDimensionalParameter([0, 1, 2, 3])
        parmConstrainedString[0] = new ConstrainedString(ITestComponentMarker, "markedComponent")
    }
}
