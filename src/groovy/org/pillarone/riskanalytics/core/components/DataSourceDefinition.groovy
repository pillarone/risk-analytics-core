package org.pillarone.riskanalytics.core.components

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber

class DataSourceDefinition {

    Parameterization parameterization
    String path
    Set<String> fields = []
    Set<Integer> periods = []
    String collectorName

    DataSourceDefinition() {

    }

    DataSourceDefinition(String paramName, String paramVersion, Class paramModelClass, String path, List<String> fields, List<Integer> periods, String collectorName) {
        this.parameterization = new Parameterization(paramName, paramModelClass)
        this.parameterization.versionNumber = new VersionNumber(paramVersion)
        this.path = path
        this.fields = fields
        this.periods = periods
        this.collectorName = collectorName
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof DataSourceDefinition) {
            return new EqualsBuilder()
                    .append(parameterization.nameAndVersion, obj.parameterization.nameAndVersion)
                    .append(parameterization.modelClass, obj.parameterization.modelClass)
                    .append(path, obj.path)
                    .append(fields.toArray(), obj.fields.toArray())
                    .append(periods.toArray(), obj.periods.toArray())
                    .append(collectorName, obj.collectorName).equals
        }

        return false
    }

    @Override
    int hashCode() {
        return new HashCodeBuilder().append(parameterization?.nameAndVersion).append(parameterization?.modelClass)
                .append(path).append(fields?.toArray())
                .append(periods?.toArray()).append(collectorName).toHashCode()
    }

    @Override
    String toString() {
        return "${parameterization.nameAndVersion} (${parameterization.modelClass.simpleName}): ${path}"
    }
}
