package org.pillarone.riskanalytics.core.model.migration

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComponentUtils
import org.pillarone.riskanalytics.core.parameterization.ComboBoxMatrixMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter
import org.pillarone.riskanalytics.core.components.IComponentMarker


class NamingMigration extends MigrationSupport {

    Class oldComponentUtils

    NamingMigration(VersionNumber from, VersionNumber to, Class modelClass) {
        super(from, to, modelClass)
        oldComponentUtils = ComponentUtils//getOldModelClassLoader().loadClass(ComponentUtils.name) //TODO: included in jar?
    }

    @Override
    void doMigrateParameterization(Model source, Model target) {
        target.injectComponentNames()
        MarkerBasedMultiDimensionalParameterCollector collector = new MarkerBasedMultiDimensionalParameterCollector()
        target.accept(collector)

        List<AbstractMultiDimensionalParameter> paramsToChange = collector.result
        paramsToChange.each {
            handleParam(it, target)
        }

    }

    private void handleParam(AbstractMultiDimensionalParameter parameter, Model target) {
        throw new IllegalArgumentException("Unexpected parameter type ${parameter.class.name}")
    }

    private void handleParam(ConstrainedMultiDimensionalParameter parameter, Model target) {
        for (int i = 0; i < parameter.columnCount; i++) {
            Class columnType = parameter.constraints.getColumnType(i)
            if (IComponentMarker.isAssignableFrom(columnType)) {
                List<Component> components = target.getMarkedComponents(columnType)

                List columnValues = parameter.values.get(i)

                List newList = []

                columnValues.each { def value ->
                    if (value != "") {
                        Component component = components.find { oldComponentUtils.getNormalizedName(it.name) == value }
                        newList << component.name
                    } else {
                        newList << ""
                    }
                }

                parameter.values.set(i, newList)
            }
        }
    }

    private void handleParam(ComboBoxTableMultiDimensionalParameter parameter, Model target) {
        Class markerClass = parameter.markerClass
        List<Component> components = target.getMarkedComponents(markerClass)

        List<List> newValues = []

        List<List> values = parameter.values
        values.each {List list ->

            List newList = []
            newValues << newList

            list.each { def value ->
                if (value != "") {
                    Component component = components.find { oldComponentUtils.getNormalizedName(it.name) == value }
                    newList << component.name
                } else {
                    newList << ""
                }
            }
        }

        values.clear()
        values.addAll(newValues)
    }

    private void handleParam(ComboBoxMatrixMultiDimensionalParameter parameter, Model target) {
        Class markerClass = parameter.markerClass
        List<Component> components = target.getMarkedComponents(markerClass)

        List rowNames = parameter.rowNames
        List columnNames = parameter.columnNames

        List newList = []

        rowNames.each { def value ->
            if (value != "") {
                Component component = components.find { oldComponentUtils.getNormalizedName(it.name) == value }
                newList << component.name
            } else {
                newList << ""
            }
        }

        rowNames.clear()
        rowNames.addAll(newList)

        columnNames.clear()
        columnNames.addAll(newList)
    }
}
