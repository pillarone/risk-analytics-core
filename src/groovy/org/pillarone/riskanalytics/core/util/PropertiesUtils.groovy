package org.pillarone.riskanalytics.core.util

import groovy.transform.CompileStatic

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
@CompileStatic
class PropertiesUtils {

    Properties getProperties(String propertiesName) {
        Properties props = new Properties()
        try {
            InputStream stream = getClass().getResourceAsStream(propertiesName)
            props.load(stream)
            return props
        } catch (Exception e) {
            return new Properties()
        }
    }


}
