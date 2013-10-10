package org.pillarone.riskanalytics.core.components;

import java.io.Serializable;

public interface IComponentMarker extends Serializable {
    public String getNormalizedName();
    public String getName();
}
