package org.pillarone.riskanalytics.core.util

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class ResourceBundleRegistryTests extends GroovyTestCase {
    static String RESOURCE = "RESOURCE"
    static String VALIDATION = "VALIDATION"
    static String HELP = "HELP"

    public void testAddBundle() {
        assertTrue ResourceBundleRegistry.getBundles(RESOURCE).isEmpty()
        assertTrue ResourceBundleRegistry.getBundles(VALIDATION).isEmpty()
        assertTrue ResourceBundleRegistry.getBundles(HELP).isEmpty()

        ResourceBundleRegistry.addBundle(RESOURCE, "test1")
        assertEquals 1, ResourceBundleRegistry.getBundles(RESOURCE).size()
        assertEquals 0, ResourceBundleRegistry.getBundles(VALIDATION).size()
        assertEquals 0, ResourceBundleRegistry.getBundles(HELP).size()


        ResourceBundleRegistry.addBundle(VALIDATION, "test2")
        assertEquals 1, ResourceBundleRegistry.getBundles(RESOURCE).size()
        assertEquals 1, ResourceBundleRegistry.getBundles(VALIDATION).size()
        assertEquals 0, ResourceBundleRegistry.getBundles(HELP).size()

        ResourceBundleRegistry.addBundle(HELP, "test3")
        assertEquals 1, ResourceBundleRegistry.getBundles(RESOURCE).size()
        assertEquals 1, ResourceBundleRegistry.getBundles(VALIDATION).size()
        assertEquals 1, ResourceBundleRegistry.getBundles(HELP).size()

    }
}
