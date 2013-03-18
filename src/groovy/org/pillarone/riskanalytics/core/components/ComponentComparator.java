package org.pillarone.riskanalytics.core.components;

import java.util.Comparator;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
public class ComponentComparator implements Comparator<Component> {
        private static ComponentComparator instance = null;

    private ComponentComparator() {
    }

    public static ComponentComparator getInstance() {
        if (instance == null) {
            instance = new ComponentComparator();
        }
        return instance;
    }

    /**
     * @return compares components based on the hash code of their name attribute
     */
    public int compare(Component component1, Component component2) {
        return  component1.getName().compareTo(component2.getName());
    }
}
