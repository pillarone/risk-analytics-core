package org.pillarone.riskanalytics.core.search


abstract class AbstractMultiValueFilter implements ISearchFilter {

    protected List<String> valueList = []

    void setValues(List<String> values) {
        clear()
        valueList.addAll(values)
    }

    List<String> getValues(){
        return valueList
    }


    void clear() {
        valueList.clear()
    }
}