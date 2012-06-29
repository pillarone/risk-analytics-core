package org.pillarone.riskanalytics.core.simulation;

import org.pillarone.riskanalytics.core.components.ComponentUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simulation exception extends a normal runtime exception with the (component) path it occurs in.
 * The path is extended i.e. by each recursive call of Component.execute(). Manually extension is also possible by using
 * the method addPathElement(). getMessage() is then enhanced with the path.
 *
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class SimulationException extends RuntimeException {

    private List<String> pathElements = new ArrayList<String>();

    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SimulationException(Throwable cause) {
        super(cause);
    }

    public void addPathElement(String pathElement) {
        // always insert at position 0 to get the path in the correct order
        pathElements.add(0, pathElement);
    }

    String path() {
        StringBuilder technicalPath = new StringBuilder();
        boolean firstPassed = false;
        for (String pathElement : pathElements) {
            if (firstPassed) {
                technicalPath.append(ComponentUtils.PATH_SEPARATOR);
            }
            else {
                firstPassed = true;
            }
            technicalPath.append(ComponentUtils.getNormalizedName(pathElement));
        }
        return technicalPath.toString();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\nPath: " + path();
    }

}
