package org.pillarone.riskanalytics.core.util

import groovy.transform.CompileStatic

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */

@CompileStatic
class ResourceBundleRegistry {

    private static Map<String, Set<String>> bundles = [:]

    static String RESOURCE = "RESOURCE"
    static String VALIDATION = "VALIDATION"
    static String HELP = "HELP"

    static synchronized void addBundle(String key, String bundleName) {
        Set<String> typeBundles = getBundles(key)
        typeBundles.add(bundleName)
    }

    public static Set<String> getBundles(String key) {
        Set<String> typeBundles = bundles.get(key)
        if (!typeBundles) {
            typeBundles = new HashSet<String>()
            bundles.put(key, typeBundles)
        }
        return typeBundles
    }
}
