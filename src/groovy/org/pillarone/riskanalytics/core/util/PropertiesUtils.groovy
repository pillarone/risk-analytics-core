package org.pillarone.riskanalytics.core.util

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
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
