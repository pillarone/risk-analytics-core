package org.pillarone.riskanalytics.core.util

public class ConfigObjectUtils {

    public static void spreadRanges(ConfigObject config) {
        def rangeKeys = [:]
        List ranges = []
        config.each {key, value ->
            if (value instanceof ConfigObject) {
                spreadRanges(value)
            }
            if (key instanceof Range) {
                ranges << key
                key.each {
                    rangeKeys[it] = value
                }
            }
        }
        config.putAll(rangeKeys)
        ranges.each {
            config.remove(it)
        }
    }

}

class PacketUtils {

    public static final Map<Class, Class> primitiveToWrapperClass = [
            (Double.TYPE): Double,
            (Integer.TYPE): Integer,
            (Float.TYPE): Float,
            (Boolean.TYPE): Boolean,
            (Short.TYPE): Short,
            (Long.TYPE): Long,
            (Byte.TYPE): Byte,
    ].asSynchronized()
}