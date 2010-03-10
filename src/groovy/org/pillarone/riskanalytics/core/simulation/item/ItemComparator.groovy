package org.pillarone.riskanalytics.core.simulation.item

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.parameterization.AbstractMultiDimensionalParameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.MultiDimensionalParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder

class ItemComparator {

    static Log LOG = LogFactory.getLog(ItemComparator)

    static boolean contentEquals(ResultConfiguration a, ResultConfiguration b) {
        if (a.collectors.size() != b.collectors.size()) return false

        List aCollectors = a.collectors.sort { it.path }
        List bCollectors = b.collectors.sort { it.path }

        for (int i = 0; i < aCollectors.size(); i++) {
            PacketCollector aCollector = aCollectors.get(i)
            PacketCollector bCollector = bCollectors.get(i)
            if (aCollector.path != bCollector.path) return false
            if (aCollector.mode.identifier != bCollector.mode.identifier) return false
        }
        return true
    }

    static boolean contentEquals(Parameterization a, Parameterization b) {
        if (a.periodCount != b.periodCount) {
            return false
        }

        if (listSize(a.parameters) != listSize(b.parameters)) {
            return false
        }

        for (ParameterHolder aParam: a.parameters) {
            ParameterHolder bParam = b.parameters.find { aParam.path == it.path && aParam.periodIndex == it.periodIndex }
            if (!bParam) {
                return false
            }
            if (!compareParameter(aParam, bParam)) {
                return false
            }
        }
        return true

    }

    private static boolean compareParameter(ParameterHolder a, ParameterHolder b) {
        return a.businessObject == b.businessObject
    }

    private static boolean compareParameter(ParameterObjectParameterHolder a, ParameterObjectParameterHolder b) {
        if (!(a.classifier == b.classifier)) {
            return false
        }

        if (listSize(a.classifierParameters) != listSize(b.classifierParameters)) {
            return false
        }

        for (Map.Entry<String, ParameterHolder> aEntry: a.classifierParameters) {
            ParameterHolder bValue = b.classifierParameters.get(aEntry.key)
            if (!bValue) {
                return false
            }
            if (!compareParameter(aEntry.value, bValue)) {
                return false
            }
        }
        return true
    }


    private static boolean compareParameter(MultiDimensionalParameterHolder a, MultiDimensionalParameterHolder b) {
        AbstractMultiDimensionalParameter aObject = a.businessObject
        AbstractMultiDimensionalParameter bObject = b.businessObject
        if (aObject.getClass().name != bObject.getClass().name) {
            return false
        }

        List aValues = aObject.values
        List bValues = bObject.values
        if(aValues.size() == 1 && aValues[0] instanceof List) {
            aValues = aValues[0]
        }
        if(bValues.size() == 1 && bValues[0] instanceof List) {
            bValues = bValues[0]
        }
        if (!aValues.equals(bValues)) {
            return false
        }

        if (!aObject.rowNames.equals(bObject.rowNames)) {
            return false
        }

        if (!aObject.columnNames.equals(bObject.columnNames)) {
            return false
        }

        return true
    }

    private static int listSize(def list) {
        if (!list) {
            return 0
        } else {
            return list.size()
        }
    }
}
