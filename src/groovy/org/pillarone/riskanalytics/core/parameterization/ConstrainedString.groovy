package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.components.Component

public class ConstrainedString implements Serializable {

    String stringValue
    Class markerClass
    Component selectedComponent

    public ConstrainedString(Class markerClass, String stringValue) {
        if (markerClass == null || stringValue == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        this.markerClass = markerClass;
        this.stringValue = stringValue;
    }

    // todo: used for export, functionality should be part of a more meaningful name.
    String toString() {
        "new org.pillarone.riskanalytics.core.parameterization.ConstrainedString(${markerClass.name}, '${stringValue}')"
    }
}
