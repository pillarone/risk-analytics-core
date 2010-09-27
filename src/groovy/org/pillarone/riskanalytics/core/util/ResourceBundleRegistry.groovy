package org.pillarone.riskanalytics.core.util

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */


class ResourceBundleRegistry {

    private static Set<String> resourceBundles = new HashSet<String>()

    static void addResourceBundle(String bundleName) {
        resourceBundles.add(bundleName)
    }

    static Set<String> getResourceBundles() {
        return resourceBundles
    }
}
