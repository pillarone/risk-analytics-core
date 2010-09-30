package org.pillarone.riskanalytics.core.simulation.item.parameter;

import java.util.List;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public interface IMarkerValueAccessor {

    /**
     * @param markerInterface
     * @param value
     * @return  list containing the model path
     */
    List<String> referencePaths(Class markerInterface, String value);

    /**
     * @param markerInterface
     * @param oldValue
     * @param newValue
     * @return list containing the model path
     */
    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue);
}
