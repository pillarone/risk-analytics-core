package org.pillarone.riskanalytics.core.util

import org.joda.time.DateTime


abstract class CloneSupport {

    public static List deepClone(List list) {
        List result = []
        for (def element in list) {
            try {
                def cloned = element.clone()
                result << cloned
            } catch (NullPointerException npe) {
                result << null
            } catch (CloneNotSupportedException cnse) {
                result << doClone(element)
            }

        }
        return result
    }

    private static def doClone(def object) {
        throw new CloneNotSupportedException(object.class.simpleName)
    }

    private static def doClone(Integer object) {
        new Integer(object.intValue())
    }

    private static def doClone(Double object) {
        new Double(object.doubleValue())
    }

    private static def doClone(Short object) {
        new Short(object.shortValue())
    }

    private static def doClone(Long object) {
        new Long(object.longValue())
    }

    private static def doClone(String object) {
        object
    }

    private static def doClone(Boolean object) {
        new Boolean(object.booleanValue())
    }

    private static def doClone(DateTime object) {
        new DateTime(object.millis)
    }
}
