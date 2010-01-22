package models.core.parameterApplicator

import org.pillarone.riskanalytics.core.model.StochasticModel
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedString
import org.pillarone.riskanalytics.core.example.marker.ITestComponentMarker
import org.pillarone.riskanalytics.core.example.component.ExampleOutputComponent

class ParameterApplicatorModel extends StochasticModel {

    ParameterApplicatorComponent input
    ExampleOutputComponent markedComponent

    void initComponents() {
        input = new ParameterApplicatorComponent()
        markedComponent = new ExampleOutputComponent()
    }

    void wireComponents() {

    }


}

class ParameterApplicatorComponent extends Component {

    SimpleMultiDimensionalParameter parmMultiDimensionalParameter = new SimpleMultiDimensionalParameter([[0, 1], [2, 3]])
    ConstrainedString parmConstrainedString = new ConstrainedString(ITestComponentMarker, "markedComponent")

    protected void doCalculation() {

    }


}