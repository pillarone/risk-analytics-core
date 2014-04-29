package org.pillarone.riskanalytics.core.parameter


class DataSourceParameter extends Parameter {

    String parameterizationName
    String parameterizationVersion
    String modelClassName

    String parameterizationPath
    String fields
    String periods
    String collectorName

    @Override
    Class persistedClass() {
        return DataSourceParameter
    }
}
